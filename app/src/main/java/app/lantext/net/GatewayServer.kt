package app.lantext.net

import android.content.Context
import android.webkit.MimeTypeMap
import app.lantext.data.PairedDevice
import app.lantext.data.PairingManager
import app.lantext.sms.AddPhoneRequest
import app.lantext.sms.ContactsRepository
import app.lantext.sms.CreateContactRequest
import app.lantext.sms.SendRequest
import app.lantext.sms.SmsRepository
import app.lantext.util.PrivateNetwork
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

class GatewayServer(
    private val context: Context,
    private val bindAddress: String,
    private val listenPort: Int,
    private val certs: TlsCertificateStore,
    private val pairing: PairingManager,
    private val sms: SmsRepository,
    private val contacts: ContactsRepository,
    private val clientCount: MutableStateFlow<Int>,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val connected = AtomicInteger(0)
    private var http: LanHttp? = null

    fun start() {
        val keyStore = certs.keyStore(bindAddress)
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, certs.password)
        val ssl = SSLContext.getInstance("TLS")
        ssl.init(kmf.keyManagers, null, SecureRandom())
        val server = LanHttp(bindAddress, listenPort)
        server.makeSecure(ssl.serverSocketFactory, null)
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
        http = server
    }

    fun stop() {
        http?.stop()
        http = null
        connected.set(0)
        clientCount.value = 0
    }

    private inner class LanHttp(host: String, port: Int) : NanoWSD(host, port) {
        override fun serve(session: IHTTPSession): Response {
            val remote = session.headers["http-client-ip"]
                ?: session.headers["remote-addr"]
                ?: session.remoteIpAddress
            if (!PrivateNetwork.isPrivateHost(remote)) {
                return json(Response.Status.FORBIDDEN, mapOf("error" to "lan_only"))
            }
            if (isWebsocketRequested(session)) {
                return super.serve(session)
            }
            return try {
                route(session)
            } catch (e: IllegalArgumentException) {
                json(Response.Status.BAD_REQUEST, mapOf("error" to (e.message ?: "bad request")))
            } catch (e: IllegalStateException) {
                json(Response.Status.BAD_REQUEST, mapOf("error" to (e.message ?: "send failed")))
            } catch (e: SecurityException) {
                json(Response.Status.FORBIDDEN, mapOf("error" to "forbidden"))
            } catch (e: Exception) {
                android.util.Log.e("LanText", "request failed", e)
                json(Response.Status.INTERNAL_ERROR, mapOf("error" to (e.message ?: "internal")))
            }
        }

        override fun openWebSocket(handshake: IHTTPSession): WebSocket {
            return EventSocket(handshake)
        }

        private fun route(session: IHTTPSession): Response {
            val uri = session.uri.substringBefore('?')
            val method = session.method
            fun q(name: String) = session.parameters[name]?.firstOrNull().orEmpty()

            if (method == Method.GET && (uri == "/" || uri.isEmpty())) {
                return asset("web/index.html", "text/html")
            }
            if (method == Method.GET && uri == "/favicon.ico") {
                return asset("web/favicon.svg", "image/svg+xml")
            }

            if (method == Method.GET && uri == "/api/v1/meta") {
                return json(
                    Response.Status.OK,
                    mapOf(
                        "fingerprint" to certs.fingerprintSha256,
                        "listening" to true,
                        "paired" to (deviceFor(session) != null),
                    ),
                )
            }

            if (method == Method.POST && uri == "/api/v1/pair") {
                val body = readJson(session)
                val pin = body["pin"] ?: ""
                val name = body["clientName"].orEmpty().ifBlank { session.headers["user-agent"]?.take(60) ?: "Browser" }
                val (ok, extra) = kotlinx.coroutines.runBlocking {
                    pairing.beginPair(
                        pin,
                        name,
                        session.headers["remote-addr"] ?: session.remoteIpAddress,
                    )
                }
                if (!ok) {
                    val status = if (extra == "locked") Response.Status.FORBIDDEN else Response.Status.UNAUTHORIZED
                    return json(status, mapOf("error" to (extra ?: "invalid")))
                }
                return json(Response.Status.ACCEPTED, mapOf("requestId" to extra))
            }

            if (method == Method.GET && uri.startsWith("/api/v1/pair/")) {
                val id = uri.removePrefix("/api/v1/pair/")
                return when (pairing.pairStatus(id)) {
                    PairingManager.PairStatus.PENDING -> json(Response.Status.OK, mapOf("status" to "pending"))
                    PairingManager.PairStatus.DENIED -> json(Response.Status.OK, mapOf("status" to "denied"))
                    PairingManager.PairStatus.APPROVED -> {
                        val token = pairing.consumeApprovedToken(id)
                            ?: return json(Response.Status.NOT_FOUND, mapOf("status" to "unknown"))
                        val res = json(Response.Status.OK, mapOf("status" to "approved", "token" to token))
                        res.addHeader(
                            "Set-Cookie",
                            "$COOKIE=$token; Path=/; Max-Age=34560000; Secure; HttpOnly; SameSite=Strict",
                        )
                        res
                    }
                    PairingManager.PairStatus.UNKNOWN ->
                        json(Response.Status.NOT_FOUND, mapOf("status" to "unknown"))
                }
            }

            if (uri.startsWith("/api/v1/") && uri != "/api/v1/events") {
                val device = deviceFor(session) ?: return json(Response.Status.UNAUTHORIZED, mapOf("error" to "unpaired"))
                kotlinx.coroutines.runBlocking { pairing.touch(device.id) }
                return api(session, uri, method)
            }

            if (method == Method.GET && !uri.startsWith("/api/")) {
                val rel = uri.trimStart('/')
                if (".." in rel) return json(Response.Status.BAD_REQUEST, mapOf("error" to "bad path"))
                val ext = rel.substringAfterLast('.', "")
                val res = asset("web/$rel", mime(ext))
                if (rel == "sw.js") {
                    res.addHeader("Service-Worker-Allowed", "/")
                }
                return res
            }
            return json(Response.Status.NOT_FOUND, mapOf("error" to "not_found"))
        }

        private fun api(session: IHTTPSession, uri: String, method: Method): Response {
            fun q(name: String) = session.parameters[name]?.firstOrNull().orEmpty()
            return kotlinx.coroutines.runBlocking {
                when {
                    method == Method.GET && uri == "/api/v1/session" ->
                        json(Response.Status.OK, mapOf("ok" to true))
                    method == Method.DELETE && uri == "/api/v1/session" -> {
                        deviceFor(session)?.let { pairing.revoke(it.id) }
                        json(Response.Status.OK, mapOf("ok" to true))
                    }
                    method == Method.GET && uri == "/api/v1/conversations" ->
                        jsonRaw(json.encodeToString(sms.conversations()))
                    method == Method.GET && uri.matches(Regex("/api/v1/conversations/[^/]+/messages")) -> {
                        val id = uri.split("/")[4]
                        val before = q("before").toLongOrNull()
                        val limit = q("limit").toIntOrNull()?.coerceIn(1, 100) ?: 40
                        jsonRaw(json.encodeToString(sms.messages(id, before, limit)))
                    }
                    method == Method.POST && uri.matches(Regex("/api/v1/conversations/[^/]+/read")) -> {
                        sms.markRead(uri.split("/")[4])
                        json(Response.Status.OK, mapOf("ok" to true))
                    }
                    method == Method.POST && uri == "/api/v1/conversations" -> {
                        val req = json.decodeFromString<SendRequest>(readBody(session))
                        jsonRaw(json.encodeToString(sendOutgoing(req, req.recipients)), 202)
                    }
                    method == Method.POST && uri.matches(Regex("/api/v1/conversations/[^/]+/messages")) -> {
                        val threadId = uri.split("/")[4]
                        val req = json.decodeFromString<SendRequest>(readBody(session))
                        val dest = req.recipients.ifEmpty { recipientsForThread(threadId) }
                        jsonRaw(json.encodeToString(sendOutgoing(req, dest)), 202)
                    }
                    method == Method.GET && uri == "/api/v1/search" ->
                        jsonRaw(json.encodeToString(sms.search(q("q"))))
                    method == Method.GET && uri == "/api/v1/contacts" -> {
                        contacts.refresh()
                        jsonRaw(json.encodeToString(contacts.search(q("q"))))
                    }
                    method == Method.POST && uri == "/api/v1/contacts" -> {
                        val req = json.decodeFromString<CreateContactRequest>(readBody(session))
                        val created = contacts.createContact(req.name, req.number)
                        sms.emitRefresh()
                        jsonRaw(json.encodeToString(created), 201)
                    }
                    method == Method.POST && uri.matches(Regex("/api/v1/contacts/[^/]+/phones")) -> {
                        val id = uri.split("/")[4]
                        val req = json.decodeFromString<AddPhoneRequest>(readBody(session))
                        val updated = contacts.addPhoneToContact(id, req.number)
                        sms.emitRefresh()
                        jsonRaw(json.encodeToString(updated))
                    }
                    method == Method.GET && uri == "/api/v1/contacts/photo" -> {
                        val bytes = contacts.photoBytes(q("number"))
                            ?: return@runBlocking notFound()
                        bytes(bytes, "image/jpeg")
                    }
                    method == Method.GET && uri.startsWith("/api/v1/media/") -> {
                        val part = sms.mediaPart(uri.removePrefix("/api/v1/media/"))
                            ?: return@runBlocking notFound()
                        bytes(part.second, part.first)
                    }
                    else -> json(Response.Status.NOT_FOUND, mapOf("error" to "not_found"))
                }
            }
        }

        private suspend fun sendOutgoing(
            req: SendRequest,
            recipients: List<String>,
        ): app.lantext.sms.MessageDto {
            val image = req.imageBase64?.takeIf { it.isNotBlank() }
            return if (image != null) {
                val bytes = android.util.Base64.decode(image, android.util.Base64.DEFAULT)
                require(bytes.isNotEmpty()) { "Picture data was empty" }
                sms.sendMms(recipients, req.body, bytes, req.imageMime, req.subscriptionId)
            } else {
                sms.sendSms(recipients, req.body, req.subscriptionId)
            }
        }

        private suspend fun recipientsForThread(threadId: String): List<String> {
            val convo = sms.conversations().firstOrNull { it.id == threadId }
            val fromList = convo?.recipients.orEmpty()
            if (fromList.isNotEmpty()) return fromList
            return convo?.address?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()
        }

        private fun deviceFor(session: IHTTPSession): PairedDevice? {
            val header = session.headers["authorization"]
            val token = when {
                header != null && header.startsWith("Bearer ", true) -> header.removePrefix("Bearer ").trim()
                else -> cookie(session, COOKIE)
            }
            return token?.let { kotlinx.coroutines.runBlocking { pairing.deviceForToken(it) } }
        }

        private fun cookie(session: IHTTPSession, name: String): String? {
            val raw = session.headers["cookie"].orEmpty()
            return raw.split(';').map { it.trim() }
                .firstOrNull { it.startsWith("$name=") }
                ?.substringAfter("=")
        }

        private fun asset(path: String, mime: String): Response {
            val stream = try {
                context.assets.open(path)
            } catch (_: Exception) {
                return json(Response.Status.NOT_FOUND, mapOf("error" to "not_found"))
            }
            val bytes = stream.use { it.readBytes() }
            val res = newFixedLengthResponse(Response.Status.OK, mime, bytes.inputStream(), bytes.size.toLong())
            secure(res, cacheable = true)
            return res
        }

        private fun json(status: Response.IStatus, body: Map<String, Any?>): Response {
            val encoded = org.json.JSONObject(body).toString()
            val res = newFixedLengthResponse(status, "application/json", encoded)
            secure(res)
            return res
        }

        private fun jsonRaw(encoded: String, code: Int = 200): Response {
            val st = Response.Status.lookup(code) ?: Response.Status.OK
            val res = newFixedLengthResponse(st, "application/json", encoded)
            secure(res)
            return res
        }

        private fun bytes(data: ByteArray, mime: String): Response {
            val res = newFixedLengthResponse(Response.Status.OK, mime, data.inputStream(), data.size.toLong())
            secure(res)
            return res
        }

        private fun notFound(): Response = json(Response.Status.NOT_FOUND, mapOf("error" to "not_found"))

        private fun secure(res: Response, cacheable: Boolean = false) {
            res.addHeader("X-Content-Type-Options", "nosniff")
            res.addHeader("X-Frame-Options", "DENY")
            res.addHeader("Referrer-Policy", "no-referrer")
            res.addHeader(
                "Content-Security-Policy",
                "default-src 'self'; img-src 'self' data: blob:; media-src 'self' blob:; " +
                    "style-src 'self' 'unsafe-inline'; script-src 'self'; worker-src 'self'; " +
                    "connect-src 'self' wss: https:; frame-ancestors 'none'; base-uri 'self'; form-action 'self'",
            )
            res.addHeader("Cache-Control", if (cacheable) "no-cache" else "no-store")
        }

        private fun readBody(session: IHTTPSession): String {
            val files = HashMap<String, String>()
            session.parseBody(files)
            val len = session.headers["content-length"]?.toIntOrNull() ?: 0
            if (files.containsKey("postData")) return files["postData"].orEmpty()
            val posted = session.parameters["postData"]?.firstOrNull()
            if (posted != null) return posted
            if (len == 0) return ""
            val buf = ByteArray(len)
            session.inputStream.read(buf)
            return String(buf, Charsets.UTF_8)
        }

        private fun readJson(session: IHTTPSession): Map<String, String> {
            val raw = readBody(session)
            if (raw.isBlank()) return emptyMap()
            val obj = org.json.JSONObject(raw)
            return obj.keys().asSequence().associateWith { obj.optString(it) }
        }

        private fun mime(ext: String): String = when (ext.lowercase()) {
            "html" -> "text/html"
            "js" -> "application/javascript"
            "css" -> "text/css"
            "svg" -> "image/svg+xml"
            "png" -> "image/png"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
        }
    }

    private inner class EventSocket(handshake: NanoHTTPD.IHTTPSession) : NanoWSD.WebSocket(handshake) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private var job: Job? = null

        override fun onOpen() {
            val header = handshakeRequest.headers["authorization"]
            val cookie = handshakeRequest.headers["cookie"].orEmpty()
            val qp = handshakeRequest.parameters["token"]?.firstOrNull()
            val token = when {
                qp != null -> qp
                header != null && header.startsWith("Bearer ", true) -> header.removePrefix("Bearer ").trim()
                else -> cookie.split(';').map { it.trim() }
                    .firstOrNull { it.startsWith("$COOKIE=") }?.substringAfter("=")
            }
            val device = token?.let { kotlinx.coroutines.runBlocking { pairing.deviceForToken(it) } }
            if (device == null) {
                close(NanoWSD.WebSocketFrame.CloseCode.PolicyViolation, "unpaired", false)
                return
            }
            clientCount.value = connected.incrementAndGet()
            job = scope.launch {
                sms.events.collect { payload ->
                    try {
                        send(payload)
                    } catch (_: Exception) {
                    }
                }
            }
        }

        override fun onClose(code: NanoWSD.WebSocketFrame.CloseCode?, reason: String?, initiatedByRemote: Boolean) {
            job?.cancel()
            scope.cancel()
            clientCount.value = connected.updateAndGet { (it - 1).coerceAtLeast(0) }
        }

        override fun onMessage(message: NanoWSD.WebSocketFrame?) = Unit
        override fun onPong(pong: NanoWSD.WebSocketFrame?) = Unit
        override fun onException(exception: java.io.IOException?) = Unit
    }

    companion object {
        const val COOKIE = "lt_session"
    }
}

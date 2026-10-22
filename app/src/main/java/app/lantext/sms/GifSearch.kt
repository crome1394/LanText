package app.lantext.sms

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * KLIPY GIF search shared by the phone picker and the computer inbox.
 * The key stays on the phone. Callers pass it in and must not log the request URL.
 */
object GifSearch {
    const val MAX_BYTES = 280_000

    fun search(query: String, apiKey: String, maxBytes: Int = MAX_BYTES): List<GifHit> {
        val key = normalizeKey(apiKey)
        val cap = maxBytes.coerceIn(1, MAX_BYTES)
        val kind = if (query.isBlank()) "trending" else "search"
        return hitsFrom(apiGet(endpoint(key, kind, query)), cap)
    }

    fun hitsFrom(json: String, maxBytes: Int): List<GifHit> {
        val cap = maxBytes.coerceIn(1, MAX_BYTES)
        val root = Json.parseToJsonElement(json).jsonObject
        val result = root["result"]?.jsonPrimitive?.booleanOrNull
        if (result == false) error("KLIPY search failed")
        val data = root["data"]
        val arr = when (data) {
            is JsonArray -> data
            is JsonObject -> data["data"] as? JsonArray
            else -> null
        } ?: return emptyList()
        val out = ArrayList<GifHit>(24)
        for (element in arr) {
            if (out.size >= 24) break
            val item = element as? JsonObject ?: continue
            val type = item.str("type")
            if (type == "ad" || type == "banner") continue
            val file = item["file"] as? JsonObject ?: continue
            val renditions = ArrayList<Rendition>()
            for ((_, quality) in file) {
                val gif = (quality as? JsonObject)?.get("gif") as? JsonObject ?: continue
                val gifUrl = gif.str("url")
                val size = gif["size"]?.jsonPrimitive?.intOrNull ?: 0
                if (size !in 1..cap || !isAllowed(gifUrl)) continue
                val width = gif["width"]?.jsonPrimitive?.intOrNull ?: 0
                val height = gif["height"]?.jsonPrimitive?.intOrNull ?: 0
                renditions += Rendition(gifUrl, size, width, height)
            }
            if (renditions.isEmpty()) continue
            val send = renditions.maxBy { it.size }
            val preview = renditions.minBy { it.size }
            val id = item.str("id").ifBlank { item.str("slug") }
            if (id.isBlank()) continue
            val title = item.str("title").ifBlank { item.str("slug") }.ifBlank { "GIF" }
            out += GifHit(
                id = id,
                title = title,
                url = preview.url,
                width = preview.width,
                height = preview.height,
                bytes = send.size,
                sendUrl = send.url,
            )
        }
        return out.distinctBy { it.id }
    }

    private fun JsonObject.str(key: String): String = this[key]?.jsonPrimitive?.contentOrNull.orEmpty()

    fun download(url: String, maxBytes: Int = MAX_BYTES): ByteArray {
        val cap = maxBytes.coerceIn(1, MAX_BYTES)
        var current = url
        repeat(3) {
            require(isAllowed(current)) { "That GIF source is not allowed" }
            val conn = open(current, "image/gif, */*;q=0.8")
            try {
                val code = conn.responseCode
                if (code in 300..399) {
                    val next = conn.getHeaderField("Location") ?: error("That GIF could not be downloaded")
                    current = URL(URL(current), next).toString()
                    return@repeat
                }
                if (code !in 200..299) error("That GIF could not be downloaded")
                val bytes = readCap(conn.inputStream, cap)
                require(bytes.size >= 6 && bytes[0] == 'G'.code.toByte() && bytes[1] == 'I'.code.toByte() && bytes[2] == 'F'.code.toByte()) {
                    "That file was not a GIF"
                }
                return bytes
            } finally {
                conn.disconnect()
            }
        }
        error("That GIF could not be downloaded")
    }

    fun isAllowed(url: String): Boolean {
        if (!url.startsWith("https://")) return false
        val host = runCatching { URL(url).host.lowercase() }.getOrNull() ?: return false
        return host == "klipy.com" || host.endsWith(".klipy.com") || host == "klipy.co" || host.endsWith(".klipy.co")
    }

    private fun normalizeKey(raw: String): String {
        val key = raw.trim()
        if (key.isEmpty()) error("Add a KLIPY API key in Settings")
        val ok = key.length in 8..200 && key.all { it.isLetterOrDigit() || it == '-' || it == '_' || it == '.' }
        if (!ok) error("That API key does not look valid")
        return key
    }

    private fun endpoint(key: String, kind: String, query: String): String = buildString {
        append("https://api.klipy.com/api/v1/")
        append(URLEncoder.encode(key, Charsets.UTF_8.name()))
        append("/gifs/")
        append(kind)
        append("?page=1&per_page=24&content_filter=medium&format_filter=gif")
        if (query.isNotBlank()) {
            append("&q=")
            append(URLEncoder.encode(query.trim().take(80), Charsets.UTF_8.name()))
        }
    }

    private fun apiGet(url: String): String {
        require(url.startsWith("https://api.klipy.com/api/v1/")) { "KLIPY search failed" }
        val conn = open(url, "application/json")
        try {
            val code = conn.responseCode
            if (code == 401 || code == 403) error("That KLIPY API key was rejected")
            if (code !in 200..299) error("KLIPY search failed")
            return String(readCap(conn.inputStream, 2_000_000, "KLIPY search failed"), Charsets.UTF_8)
        } finally {
            conn.disconnect()
        }
    }

    private fun open(url: String, accept: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = false
            connectTimeout = 8_000
            readTimeout = 12_000
            setRequestProperty("Accept", accept)
            setRequestProperty("User-Agent", "Messages/0.1")
        }

    private fun readCap(input: InputStream, maxBytes: Int, tooBig: String = "That GIF is too large to send."): ByteArray {
        val out = ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        var total = 0
        input.use { stream ->
            while (true) {
                val n = stream.read(chunk)
                if (n < 0) break
                total += n
                if (total > maxBytes) error(tooBig)
                out.write(chunk, 0, n)
            }
        }
        return out.toByteArray()
    }

    private data class Rendition(val url: String, val size: Int, val width: Int, val height: Int)
}

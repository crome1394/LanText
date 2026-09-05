package app.lantext.sms

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import app.lantext.util.PhoneNumbers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SmsRepository(
    private val context: Context,
    private val contacts: ContactsRepository,
) {
    private val region = PhoneNumbers.defaultRegion(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val events: SharedFlow<String> = _events

    private var observerThread: HandlerThread? = null
    private var observer: ContentObserver? = null

    suspend fun conversations(): List<ConversationDto> = withContext(Dispatchers.IO) {
        contacts.refresh()
        val resolver = context.contentResolver
        val threads = LinkedHashMap<Long, ConversationAcc>()
        resolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.READ,
                Telephony.Sms.TYPE,
            ),
            null,
            null,
            "${Telephony.Sms.DATE} DESC",
        )?.use { cursor ->
            val iId = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val iThread = cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val iAddress = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val iBody = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val iDate = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val iRead = cursor.getColumnIndexOrThrow(Telephony.Sms.READ)
            val iType = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            while (cursor.moveToNext()) {
                val threadId = cursor.getLong(iThread)
                val acc = threads.getOrPut(threadId) {
                    ConversationAcc(threadId = threadId)
                }
                val address = cursor.getString(iAddress).orEmpty()
                if (acc.address.isBlank()) acc.address = address
                if (acc.snippet.isBlank()) {
                    acc.snippet = cursor.getString(iBody).orEmpty()
                    acc.timestamp = cursor.getLong(iDate)
                }
                if (cursor.getInt(iType) == Telephony.Sms.MESSAGE_TYPE_INBOX && cursor.getInt(iRead) == 0) {
                    acc.unread += 1
                }
                acc.ids += cursor.getLong(iId)
            }
        }

        appendMmsSnippets(resolver, threads)
        threads.values
            .sortedByDescending { it.timestamp }
            .map { it.toDto(contacts, region) }
    }

    suspend fun messages(threadId: String, before: Long?, limit: Int): List<MessageDto> = withContext(Dispatchers.IO) {
        contacts.refresh()
        val id = threadId.toLongOrNull() ?: return@withContext emptyList()
        val out = mutableListOf<MessageDto>()
        val selection = buildString {
            append("${Telephony.Sms.THREAD_ID}=?")
            if (before != null && before > 0) append(" AND ${Telephony.Sms.DATE}<?")
        }
        val args = if (before != null && before > 0) arrayOf(threadId, before.toString()) else arrayOf(threadId)
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.STATUS,
            ),
            selection,
            args,
            "${Telephony.Sms.DATE} DESC",
        )?.use { cursor ->
            val iId = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val iThread = cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val iAddress = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val iBody = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val iDate = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val iType = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            while (cursor.moveToNext() && out.size < limit) {
                val address = cursor.getString(iAddress).orEmpty()
                val type = cursor.getInt(iType)
                out += MessageDto(
                    id = "sms-${cursor.getLong(iId)}",
                    threadId = cursor.getLong(iThread).toString(),
                    address = address,
                    displayName = contacts.displayName(address),
                    body = cursor.getString(iBody).orEmpty(),
                    timestamp = cursor.getLong(iDate),
                    incoming = type == Telephony.Sms.MESSAGE_TYPE_INBOX,
                    type = "sms",
                    status = statusName(type),
                )
            }
        }
        out += loadMms(id, before, limit)
        out.sortedByDescending { it.timestamp }.take(limit)
    }

    suspend fun search(query: String): List<SearchHit> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.length < 2) return@withContext emptyList()
        contacts.refresh()
        val convos = conversations()
        val hits = mutableListOf<SearchHit>()
        for (c in convos) {
            if (c.displayName.contains(q, true) || c.address.contains(q, true) || c.snippet.contains(q, true)) {
                hits += SearchHit(c, null)
            }
        }
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
            ),
            "${Telephony.Sms.BODY} LIKE ?",
            arrayOf("%$q%"),
            "${Telephony.Sms.DATE} DESC",
        )?.use { cursor ->
            var n = 0
            val iId = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val iThread = cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val iAddress = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val iBody = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val iDate = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val iType = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            while (cursor.moveToNext() && n < 40) {
                val thread = cursor.getLong(iThread).toString()
                val convo = convos.firstOrNull { it.id == thread } ?: continue
                val address = cursor.getString(iAddress).orEmpty()
                val type = cursor.getInt(iType)
                hits += SearchHit(
                    convo,
                    MessageDto(
                        id = "sms-${cursor.getLong(iId)}",
                        threadId = thread,
                        address = address,
                        displayName = contacts.displayName(address),
                        body = cursor.getString(iBody).orEmpty(),
                        timestamp = cursor.getLong(iDate),
                        incoming = type == Telephony.Sms.MESSAGE_TYPE_INBOX,
                        type = "sms",
                        status = statusName(type),
                    ),
                )
                n++
            }
        }
        hits.distinctBy { it.message?.id ?: "c-${it.conversation.id}" }.take(50)
    }

    suspend fun sendSms(recipients: List<String>, body: String, subscriptionId: Int?): MessageDto {
        val text = body.trim()
        require(text.isNotEmpty()) { "Message is empty" }
        val dest = recipients.map { PhoneNumbers.normalize(it, region).ifBlank { it.trim() } }
            .filter { it.isNotBlank() }
        require(dest.isNotEmpty()) { "No recipients" }
        val manager = smsManager(subscriptionId)
        withContext(Dispatchers.IO) {
            for (address in dest) {
                val parts = manager.divideMessage(text)
                if (parts.size == 1) {
                    manager.sendTextMessage(address, null, text, null, null)
                } else {
                    manager.sendMultipartTextMessage(address, null, parts, null, null)
                }
            }
        }
        val address = dest.joinToString(", ")
        return MessageDto(
            id = "local-${System.currentTimeMillis()}",
            threadId = "",
            address = address,
            displayName = contacts.displayName(dest.first()),
            body = text,
            timestamp = System.currentTimeMillis(),
            incoming = false,
            type = "sms",
            status = "sending",
        )
    }

    suspend fun sendMms(
        recipients: List<String>,
        body: String,
        imageBytes: ByteArray?,
        imageMime: String?,
        subscriptionId: Int?,
    ): MessageDto = withContext(Dispatchers.IO) {
        val dest = recipients.map { PhoneNumbers.normalize(it, region).ifBlank { it.trim() } }
            .filter { it.isNotBlank() }
        require(dest.isNotEmpty()) { "No recipients" }
        require(imageBytes != null && imageBytes.isNotEmpty()) { "MMS requires an attachment" }
        val mime = imageMime ?: "image/jpeg"
        val subId = subscriptionId ?: android.telephony.SubscriptionManager.getDefaultSmsSubscriptionId()
        MmsSender.send(context, smsManager(subscriptionId), dest, body, imageBytes, mime, subId)
        val label = when {
            mime.contains("gif", true) -> "GIF"
            mime.startsWith("audio/") -> "Voice message"
            else -> "Picture"
        }
        MessageDto(
            id = "local-mms-${System.currentTimeMillis()}",
            threadId = "",
            address = dest.joinToString(", "),
            displayName = contacts.displayName(dest.first()),
            body = body.ifBlank { label },
            timestamp = System.currentTimeMillis(),
            incoming = false,
            type = "mms",
            status = "sent",
            attachments = emptyList(),
        )
    }

    fun markRead(threadId: String) {
        try {
            val values = ContentValues().apply { put(Telephony.Sms.READ, 1) }
            context.contentResolver.update(
                Telephony.Sms.CONTENT_URI,
                values,
                "${Telephony.Sms.THREAD_ID}=? AND ${Telephony.Sms.READ}=0",
                arrayOf(threadId),
            )
        } catch (_: SecurityException) {
            // Not the default SMS app; computer-side read is still tracked in the UI.
        }
    }

    fun mediaPart(partId: String): Pair<String, ByteArray>? {
        val id = partId.toLongOrNull() ?: return null
        val uri = Uri.parse("content://mms/part/$id")
        val mime = context.contentResolver.query(uri, arrayOf(Telephony.Mms.Part.CONTENT_TYPE), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null }
            ?: return null
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        return mime to bytes
    }

    fun startWatching() {
        if (observer != null) return
        val thread = HandlerThread("lantext-sms").also { it.start() }
        observerThread = thread
        val obs = object : ContentObserver(Handler(thread.looper)) {
            override fun onChange(selfChange: Boolean) {
                emitRefresh()
            }
        }
        observer = obs
        context.contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, obs)
        context.contentResolver.registerContentObserver(Telephony.Mms.CONTENT_URI, true, obs)
    }

    fun stopWatching() {
        observer?.let { context.contentResolver.unregisterContentObserver(it) }
        observer = null
        observerThread?.quitSafely()
        observerThread = null
    }

    fun emitRefresh() {
        scope.launch {
            val payload = JSONObject().put("type", "inbox_changed").toString()
            _events.emit(payload)
        }
    }

    fun emitIncoming(address: String, body: String) {
        scope.launch {
            contacts.refresh()
            val payload = JSONObject()
                .put("type", "incoming")
                .put("address", address)
                .put("displayName", contacts.displayName(address))
                .put("body", body)
                .put("timestamp", System.currentTimeMillis())
                .toString()
            _events.emit(payload)
        }
    }

    private fun smsManager(subscriptionId: Int?): SmsManager {
        val id = subscriptionId ?: SubscriptionManager.getDefaultSmsSubscriptionId()
        if (id == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            return if (android.os.Build.VERSION.SDK_INT >= 23) {
                context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
            } else {
                SmsManager.getDefault()
            }
        }
        return if (android.os.Build.VERSION.SDK_INT >= 31) {
            context.getSystemService(SmsManager::class.java).createForSubscriptionId(id)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getSmsManagerForSubscriptionId(id)
        }
    }

    private fun appendMmsSnippets(resolver: ContentResolver, threads: LinkedHashMap<Long, ConversationAcc>) {
        resolver.query(
            Telephony.Mms.CONTENT_URI,
            arrayOf(
                Telephony.Mms._ID,
                Telephony.Mms.THREAD_ID,
                Telephony.Mms.DATE,
                Telephony.Mms.READ,
                Telephony.Mms.MESSAGE_BOX,
            ),
            null,
            null,
            "${Telephony.Mms.DATE} DESC",
        )?.use { cursor ->
            val iId = cursor.getColumnIndexOrThrow(Telephony.Mms._ID)
            val iThread = cursor.getColumnIndexOrThrow(Telephony.Mms.THREAD_ID)
            val iDate = cursor.getColumnIndexOrThrow(Telephony.Mms.DATE)
            val iRead = cursor.getColumnIndexOrThrow(Telephony.Mms.READ)
            val iBox = cursor.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_BOX)
            while (cursor.moveToNext()) {
                val threadId = cursor.getLong(iThread)
                val acc = threads.getOrPut(threadId) { ConversationAcc(threadId = threadId) }
                val date = cursor.getLong(iDate) * 1000
                if (date > acc.timestamp) {
                    acc.timestamp = date
                    acc.snippet = acc.snippet.ifBlank { "Media message" }
                }
                if (cursor.getInt(iBox) == Telephony.Mms.MESSAGE_BOX_INBOX && cursor.getInt(iRead) == 0) {
                    acc.unread += 1
                }
                if (acc.address.isBlank()) {
                    acc.address = mmsAddress(cursor.getLong(iId)).orEmpty()
                }
            }
        }
    }

    private fun loadMms(threadId: Long, before: Long?, limit: Int): List<MessageDto> {
        val out = mutableListOf<MessageDto>()
        val selection = buildString {
            append("${Telephony.Mms.THREAD_ID}=?")
            if (before != null && before > 0) append(" AND ${Telephony.Mms.DATE}<?")
        }
        val args = if (before != null && before > 0) {
            arrayOf(threadId.toString(), (before / 1000).toString())
        } else {
            arrayOf(threadId.toString())
        }
        context.contentResolver.query(
            Telephony.Mms.CONTENT_URI,
            arrayOf(
                Telephony.Mms._ID,
                Telephony.Mms.THREAD_ID,
                Telephony.Mms.DATE,
                Telephony.Mms.MESSAGE_BOX,
            ),
            selection,
            args,
            "${Telephony.Mms.DATE} DESC",
        )?.use { cursor ->
            val iId = cursor.getColumnIndexOrThrow(Telephony.Mms._ID)
            val iThread = cursor.getColumnIndexOrThrow(Telephony.Mms.THREAD_ID)
            val iDate = cursor.getColumnIndexOrThrow(Telephony.Mms.DATE)
            val iBox = cursor.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_BOX)
            while (cursor.moveToNext() && out.size < limit) {
                val mmsId = cursor.getLong(iId)
                val address = mmsAddress(mmsId).orEmpty()
                val box = cursor.getInt(iBox)
                val parts = mmsParts(mmsId)
                val text = parts.firstOrNull { it.mimeType.startsWith("text/") }?.text.orEmpty()
                val attachments = parts.filter {
                    it.mimeType.startsWith("image/") ||
                        it.mimeType.startsWith("video/") ||
                        it.mimeType.startsWith("audio/")
                }
                    .map {
                        AttachmentDto(
                            id = it.id,
                            mimeType = it.mimeType,
                            name = it.name,
                            url = "/api/v1/media/${it.id}",
                        )
                    }
                out += MessageDto(
                    id = "mms-$mmsId",
                    threadId = cursor.getLong(iThread).toString(),
                    address = address,
                    displayName = contacts.displayName(address),
                    body = text.ifBlank { if (attachments.isNotEmpty()) "" else "Media message" },
                    timestamp = cursor.getLong(iDate) * 1000,
                    incoming = box == Telephony.Mms.MESSAGE_BOX_INBOX,
                    type = "mms",
                    status = if (box == Telephony.Mms.MESSAGE_BOX_SENT) "sent" else "received",
                    attachments = attachments,
                )
            }
        }
        return out
    }

    private fun mmsAddress(mmsId: Long): String? {
        val uri = Uri.parse("content://mms/$mmsId/addr")
        context.contentResolver.query(
            uri,
            arrayOf(Telephony.Mms.Addr.ADDRESS, Telephony.Mms.Addr.TYPE),
            null,
            null,
            null,
        )?.use { cursor ->
            val iAddr = cursor.getColumnIndexOrThrow(Telephony.Mms.Addr.ADDRESS)
            val iType = cursor.getColumnIndexOrThrow(Telephony.Mms.Addr.TYPE)
            while (cursor.moveToNext()) {
                val type = cursor.getInt(iType)
                val addr = cursor.getString(iAddr).orEmpty()
                if (addr.isBlank() || addr == "insert-address-token") continue
                if (type == 137 || type == 151) return addr
            }
        }
        return null
    }

    private data class MmsPart(val id: String, val mimeType: String, val name: String?, val text: String?)

    private fun mmsParts(mmsId: Long): List<MmsPart> {
        val uri = Uri.parse("content://mms/$mmsId/part")
        val out = mutableListOf<MmsPart>()
        context.contentResolver.query(
            uri,
            arrayOf(
                Telephony.Mms.Part._ID,
                Telephony.Mms.Part.CONTENT_TYPE,
                Telephony.Mms.Part.NAME,
                Telephony.Mms.Part.TEXT,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            val iId = cursor.getColumnIndexOrThrow(Telephony.Mms.Part._ID)
            val iCt = cursor.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_TYPE)
            val iName = cursor.getColumnIndexOrThrow(Telephony.Mms.Part.NAME)
            val iText = cursor.getColumnIndexOrThrow(Telephony.Mms.Part.TEXT)
            while (cursor.moveToNext()) {
                val mime = cursor.getString(iCt).orEmpty()
                if (mime == "application/smil") continue
                out += MmsPart(
                    id = cursor.getLong(iId).toString(),
                    mimeType = mime,
                    name = cursor.getString(iName),
                    text = cursor.getString(iText),
                )
            }
        }
        return out
    }

    private fun statusName(type: Int): String = when (type) {
        Telephony.Sms.MESSAGE_TYPE_INBOX -> "received"
        Telephony.Sms.MESSAGE_TYPE_SENT -> "sent"
        Telephony.Sms.MESSAGE_TYPE_OUTBOX, Telephony.Sms.MESSAGE_TYPE_QUEUED -> "sending"
        Telephony.Sms.MESSAGE_TYPE_FAILED -> "failed"
        Telephony.Sms.MESSAGE_TYPE_DRAFT -> "draft"
        else -> "unknown"
    }

    private class ConversationAcc(
        val threadId: Long,
        var address: String = "",
        var snippet: String = "",
        var timestamp: Long = 0L,
        var unread: Int = 0,
        val ids: MutableList<Long> = mutableListOf(),
    ) {
        fun toDto(contacts: ContactsRepository, region: String): ConversationDto {
            val contact = contacts.lookup(address)
            val name = contact?.name ?: PhoneNumbers.display(address, region).ifBlank { address }
            return ConversationDto(
                id = threadId.toString(),
                address = address,
                displayName = name,
                photoUrl = contacts.photoUrl(address),
                snippet = snippet,
                timestamp = timestamp,
                unread = unread,
                isGroup = address.contains(","),
                recipients = address.split(",").map { it.trim() }.filter { it.isNotBlank() },
                avatarColor = contact?.color ?: ContactsRepository.avatarColor(name.ifBlank { address }),
                contactId = contact?.id,
            )
        }
    }
}

package app.lantext.sms

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.BlockedNumberContract
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import java.util.concurrent.atomic.AtomicInteger
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
    private val sentRequestCodes = AtomicInteger(3_000)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val events: SharedFlow<String> = _events

    private fun appName(): String =
        (context.applicationContext as? app.lantext.LanTextApp)?.productName() ?: "this app"

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
                val smsType = cursor.getInt(iType)
                if (acc.snippet.isBlank()) {
                    acc.snippet = cursor.getString(iBody).orEmpty()
                    acc.timestamp = cursor.getLong(iDate)
                }
                if (smsType != Telephony.Sms.MESSAGE_TYPE_DRAFT && !acc.sawLiveSms) {
                    acc.sawLiveSms = true
                    acc.liveSmsAt = cursor.getLong(iDate)
                }
                if (smsType == Telephony.Sms.MESSAGE_TYPE_INBOX && cursor.getInt(iRead) == 0) {
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
                Telephony.Sms.SUBSCRIPTION_ID,
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
            val iSub = cursor.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)
            while (cursor.moveToNext() && out.size < limit) {
                val address = cursor.getString(iAddress).orEmpty()
                val type = cursor.getInt(iType)
                val sub = if (iSub >= 0) cursor.getInt(iSub) else -1
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
                    simSlot = slotFor(sub),
                )
            }
        }
        out += loadMms(id, before, limit)
        orderEchoes(out, { it.id }, { it.threadId }, { it.body }, { it.timestamp }, { it.incoming })
            .asReversed()
            .take(limit)
    }

    suspend fun search(query: String): List<SearchHit> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.length < 2) return@withContext emptyList()
        contacts.refresh()
        val convos = conversations()
        val byId = convos.associateBy { it.id }
        val hits = mutableListOf<SearchHit>()
        for (c in convos) {
            if (c.displayName.contains(q, true) || c.address.contains(q, true) || c.snippet.contains(q, true)) {
                hits += SearchHit(c, null)
            }
        }
        val like = "%$q%"
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
            "${Telephony.Sms.BODY} LIKE ? COLLATE NOCASE",
            arrayOf(like),
            "${Telephony.Sms.DATE} DESC",
        )?.use { cursor ->
            var n = 0
            val iId = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val iThread = cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val iAddress = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val iBody = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val iDate = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val iType = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            while (cursor.moveToNext() && n < 200) {
                val thread = cursor.getLong(iThread).toString()
                val convo = byId[thread] ?: continue
                val address = cursor.getString(iAddress).orEmpty()
                val type = cursor.getInt(iType)
                val body = cursor.getString(iBody).orEmpty()
                hits += SearchHit(
                    convo.copy(snippet = body),
                    MessageDto(
                        id = "sms-${cursor.getLong(iId)}",
                        threadId = thread,
                        address = address,
                        displayName = contacts.displayName(address),
                        body = body,
                        timestamp = cursor.getLong(iDate),
                        incoming = type == Telephony.Sms.MESSAGE_TYPE_INBOX,
                        type = "sms",
                        status = statusName(type),
                    ),
                )
                n++
            }
        }
        searchMmsText(q, byId, hits)
        hits.distinctBy { it.message?.id ?: "c-${it.conversation.id}" }.take(80)
    }

    fun participants(threadId: String, fallbackAddress: String): List<String> {
        val id = threadId.toLongOrNull() ?: return fallbackAddress.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val fromThread = canonicalAddresses(id)
        if (fromThread.isNotEmpty()) return fromThread
        return fallbackAddress.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    private fun searchMmsText(q: String, byId: Map<String, ConversationDto>, hits: MutableList<SearchHit>) {
        val like = "%$q%"
        context.contentResolver.query(
            Uri.parse("content://mms/part"),
            arrayOf("_id", Telephony.Mms.Part.MSG_ID, Telephony.Mms.Part.TEXT, Telephony.Mms.Part.CONTENT_TYPE),
            "${Telephony.Mms.Part.TEXT} LIKE ? COLLATE NOCASE",
            arrayOf(like),
            null,
        )?.use { cursor ->
            var n = 0
            while (cursor.moveToNext() && n < 80) {
                val mime = cursor.getString(3).orEmpty()
                if (mime.startsWith("image/") || mime == "application/smil") continue
                val text = cursor.getString(2).orEmpty()
                if (text.isBlank()) continue
                val msgId = cursor.getLong(1)
                val thread = mmsThreadId(msgId) ?: continue
                val convo = byId[thread.toString()] ?: continue
                hits += SearchHit(
                    convo.copy(snippet = text),
                    MessageDto(
                        id = "mms-$msgId",
                        threadId = thread.toString(),
                        address = convo.address,
                        displayName = convo.displayName,
                        body = text,
                        timestamp = convo.timestamp,
                        incoming = true,
                        type = "mms",
                        status = "received",
                    ),
                )
                n++
            }
        }
    }

    private fun mmsThreadId(mmsId: Long): Long? {
        return context.contentResolver.query(
            Telephony.Mms.CONTENT_URI,
            arrayOf(Telephony.Mms.THREAD_ID),
            "${Telephony.Mms._ID}=?",
            arrayOf(mmsId.toString()),
            null,
        )?.use { if (it.moveToFirst()) it.getLong(0) else null }
    }

    private fun canonicalAddresses(threadId: Long): List<String> {
        val ids = context.contentResolver.query(
            Uri.parse("content://mms-sms/conversations").buildUpon()
                .appendQueryParameter("simple", "true").build(),
            arrayOf("_id", "recipient_ids"),
            "_id=?",
            arrayOf(threadId.toString()),
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) null
            else cursor.getString(1)
        } ?: return emptyList()
        return ids.split(" ").mapNotNull { raw ->
            val id = raw.trim()
            if (id.isEmpty()) return@mapNotNull null
            context.contentResolver.query(
                Uri.parse("content://mms-sms/canonical-addresses"),
                arrayOf("address"),
                "_id=?",
                arrayOf(id),
                null,
            )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        }.map { it.trim() }.filter { it.isNotBlank() }
    }

    suspend fun sendSms(recipients: List<String>, body: String, subscriptionId: Int?): MessageDto {
        val text = body.trim()
        require(text.isNotEmpty()) { "Message is empty" }
        val dest = recipients.map { PhoneNumbers.normalize(it, region).ifBlank { it.trim() } }
            .filter { it.isNotBlank() }
        require(dest.isNotEmpty()) { "No recipients" }
        val manager = smsManager(subscriptionId)
        val stored = withContext(Dispatchers.IO) {
            dest.map { address ->
                val row = storeOutgoingSms(address, text, subscriptionId)
                val parts = manager.divideMessage(text)
                val intents = ArrayList<PendingIntent>(parts.size.coerceAtLeast(1))
                val count = parts.size.coerceAtLeast(1)
                repeat(count) { index ->
                    intents += sentIntent(row.uri, index, count)
                }
                SentParts.begin(row.uri.toString(), count)
                try {
                    if (parts.size <= 1) {
                        manager.sendTextMessage(address, null, text, intents.firstOrNull(), null)
                    } else {
                        manager.sendMultipartTextMessage(address, null, parts, intents, null)
                    }
                } catch (t: Throwable) {
                    markSms(row.uri, Telephony.Sms.MESSAGE_TYPE_FAILED)
                    SentParts.takeFailed(row.uri.toString())
                    throw t
                }
                row
            }
        }
        val first = stored.first()
        return MessageDto(
            id = "sms-${first.uri.lastPathSegment}",
            threadId = first.threadId.toString(),
            address = dest.joinToString(", "),
            displayName = contacts.displayName(dest.first()),
            body = text,
            timestamp = System.currentTimeMillis(),
            incoming = false,
            type = "sms",
            status = "sent",
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
        val reports = (context.applicationContext as? app.lantext.LanTextApp)?.mmsDeliveryReports == true
        val outbound = MmsSender.send(context, smsManager(subscriptionId), dest, body, imageBytes, mime, subId, reports)
        val threadId = Telephony.Threads.getOrCreateThreadId(context, dest.toHashSet())
        val storedId = try {
            if (recentMmsExists(threadId, body)) null
            else insertOutgoingMms(threadId, dest, body, outbound.bytes, outbound.mime, subId)
        } catch (t: Throwable) {
            android.util.Log.w("LanText", "Sent MMS was not copied into the conversation", t)
            null
        }
        val label = when {
            mime.contains("gif", true) -> "GIF"
            mime.startsWith("audio/") -> "Voice message"
            else -> "Picture"
        }
        MessageDto(
            id = storedId?.let { "mms-$it" } ?: "local-mms-${System.currentTimeMillis()}",
            threadId = threadId.toString(),
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

    private data class StoredSms(val uri: Uri, val threadId: Long)

    /** Writes the sent row the default SMS app has to store itself. The radio does not. */
    private fun storeOutgoingSms(address: String, body: String, subscriptionId: Int?): StoredSms {
        val threadId = Telephony.Threads.getOrCreateThreadId(context, address)
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, System.currentTimeMillis())
            put(Telephony.Sms.READ, 1)
            put(Telephony.Sms.SEEN, 1)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
            put(Telephony.Sms.THREAD_ID, threadId)
            val id = subscriptionId ?: SubscriptionManager.INVALID_SUBSCRIPTION_ID
            if (id != SubscriptionManager.INVALID_SUBSCRIPTION_ID) put(Telephony.Sms.SUBSCRIPTION_ID, id)
        }
        val uri = try {
            context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
        } catch (_: SecurityException) {
            null
        } ?: throw IllegalStateException("Set ${appName()} as the default SMS app to send.")
        return StoredSms(uri, threadId)
    }

    private fun sentIntent(uri: Uri, index: Int, count: Int): PendingIntent {
        val intent = Intent(context, SmsSentReceiver::class.java).apply {
            action = SmsSentReceiver.ACTION_SENT
            data = Uri.parse("lantext://sms-sent/${uri.lastPathSegment}/$index")
            putExtra(SmsSentReceiver.EXTRA_URI, uri.toString())
            putExtra(SmsSentReceiver.EXTRA_INDEX, index)
            putExtra(SmsSentReceiver.EXTRA_COUNT, count)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, sentRequestCodes.incrementAndGet(), intent, flags)
    }

    private fun markSms(uri: Uri, type: Int) {
        val values = ContentValues().apply { put(Telephony.Sms.TYPE, type) }
        try {
            context.contentResolver.update(uri, values, null, null)
        } catch (_: SecurityException) {
        }
    }

    private fun recentMmsExists(threadId: Long, body: String): Boolean {
        val since = (System.currentTimeMillis() / 1000) - if (body.isBlank()) 20 else 180
        val ids = mutableListOf<Long>()
        context.contentResolver.query(
            Telephony.Mms.CONTENT_URI,
            arrayOf(Telephony.Mms._ID),
            "${Telephony.Mms.THREAD_ID}=? AND ${Telephony.Mms.DATE}>=?",
            arrayOf(threadId.toString(), since.toString()),
            null,
        )?.use { cursor ->
            val id = cursor.getColumnIndexOrThrow(Telephony.Mms._ID)
            while (cursor.moveToNext()) ids += cursor.getLong(id)
        }
        if (ids.isEmpty()) return false
        if (body.isBlank()) return ids.any { mmsHasMedia(it) }
        val needle = body.take(80)
        return ids.any { mmsText(it).contains(needle) }
    }

    private fun mmsText(mmsId: Long): String {
        val out = StringBuilder()
        context.contentResolver.query(
            Uri.parse("content://mms/$mmsId/part"),
            arrayOf(Telephony.Mms.Part.CONTENT_TYPE, Telephony.Mms.Part.TEXT),
            null,
            null,
            null,
        )?.use { cursor ->
            val type = cursor.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_TYPE)
            val text = cursor.getColumnIndexOrThrow(Telephony.Mms.Part.TEXT)
            while (cursor.moveToNext()) {
                if (cursor.getString(type).orEmpty().startsWith("text/")) {
                    out.append(cursor.getString(text).orEmpty())
                }
            }
        }
        return out.toString()
    }

    private fun mmsHasMedia(mmsId: Long): Boolean {
        var found = false
        context.contentResolver.query(
            Uri.parse("content://mms/$mmsId/part"),
            arrayOf(Telephony.Mms.Part.CONTENT_TYPE),
            null,
            null,
            null,
        )?.use { cursor ->
            val type = cursor.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_TYPE)
            while (cursor.moveToNext()) {
                val mime = cursor.getString(type).orEmpty()
                if (mime.startsWith("image/") || mime.startsWith("audio/") || mime.startsWith("video/")) found = true
            }
        }
        return found
    }

    private fun insertOutgoingMms(
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachment: ByteArray?,
        mime: String?,
        subId: Int,
    ): Long? {
        val values = ContentValues().apply {
            put(Telephony.Mms.THREAD_ID, threadId)
            put(Telephony.Mms.DATE, System.currentTimeMillis() / 1000)
            put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_SENT)
            put(Telephony.Mms.READ, 1)
            put(Telephony.Mms.SEEN, 1)
            put(Telephony.Mms.MESSAGE_TYPE, 128)
            put(Telephony.Mms.CONTENT_TYPE, "application/vnd.wap.multipart.related")
            put(Telephony.Mms.TEXT_ONLY, if (attachment == null) 1 else 0)
            put(Telephony.Mms.MESSAGE_SIZE, attachment?.size ?: body.length)
            if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) put(Telephony.Mms.SUBSCRIPTION_ID, subId)
        }
        val uri = context.contentResolver.insert(Telephony.Mms.Sent.CONTENT_URI, values) ?: return null
        val id = uri.lastPathSegment?.toLongOrNull() ?: return null
        for (address in addresses) {
            val addr = ContentValues().apply {
                put(Telephony.Mms.Addr.ADDRESS, address)
                put(Telephony.Mms.Addr.TYPE, 151)
                put(Telephony.Mms.Addr.CHARSET, 106)
            }
            context.contentResolver.insert(Uri.parse("content://mms/$id/addr"), addr)
        }
        if (body.isNotBlank()) {
            val part = ContentValues().apply {
                put(Telephony.Mms.Part.CONTENT_TYPE, "text/plain")
                put(Telephony.Mms.Part.CHARSET, 106)
                put(Telephony.Mms.Part.TEXT, body)
                put(Telephony.Mms.Part.CONTENT_LOCATION, "text_0.txt")
            }
            context.contentResolver.insert(Uri.parse("content://mms/$id/part"), part)
        }
        if (attachment != null) {
            val kind = mime ?: "image/jpeg"
            val ext = when {
                kind.contains("gif") -> "gif"
                kind.contains("png") -> "png"
                kind.contains("webp") -> "webp"
                kind.startsWith("audio/") -> "amr"
                kind.startsWith("video/") -> "mp4"
                else -> "jpg"
            }
            val part = ContentValues().apply {
                put(Telephony.Mms.Part.CONTENT_TYPE, kind)
                put(Telephony.Mms.Part.NAME, "media.$ext")
                put(Telephony.Mms.Part.CONTENT_LOCATION, "media.$ext")
            }
            val partUri = context.contentResolver.insert(Uri.parse("content://mms/$id/part"), part)
            if (partUri != null) {
                context.contentResolver.openOutputStream(partUri)?.use { it.write(attachment) }
            }
        }
        return id
    }

    fun markRead(threadId: String) {
        try {
            val sms = ContentValues().apply {
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.SEEN, 1)
            }
            context.contentResolver.update(
                Telephony.Sms.CONTENT_URI,
                sms,
                "${Telephony.Sms.THREAD_ID}=?",
                arrayOf(threadId),
            )
            val mms = ContentValues().apply {
                put(Telephony.Mms.READ, 1)
                put(Telephony.Mms.SEEN, 1)
            }
            context.contentResolver.update(
                Telephony.Mms.CONTENT_URI,
                mms,
                "${Telephony.Mms.THREAD_ID}=?",
                arrayOf(threadId),
            )
        } catch (_: SecurityException) {
            return
        }
        emitRefresh()
    }

    fun deleteConversation(threadId: String): Boolean {
        val id = threadId.toLongOrNull() ?: return false
        return try {
            context.contentResolver.delete(Uri.parse("content://mms-sms/conversations/$id"), null, null)
            emitRefresh()
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun deleteMessage(messageId: String): Boolean {
        val ref = parseStoredMessageId(messageId) ?: return false
        val base = if (ref.mms) Telephony.Mms.CONTENT_URI else Telephony.Sms.CONTENT_URI
        return try {
            context.contentResolver.delete(ContentUris.withAppendedId(base, ref.id), null, null)
            emitRefresh()
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun mediaPart(partId: String): Pair<String, ByteArray>? {
        val id = partId.toLongOrNull() ?: return null
        val uri = Uri.parse("content://mms/part/$id")
        val row = context.contentResolver.query(
            uri,
            arrayOf(Telephony.Mms.Part.CONTENT_TYPE, Telephony.Mms.Part.NAME, Telephony.Mms.Part.CONTENT_LOCATION),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) null else Triple(
                cursor.getString(0).orEmpty(),
                cursor.getString(1),
                cursor.getString(2),
            )
        } ?: return null
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val name = row.second?.ifBlank { row.third }
        val mime = displayMime(row.first, name, bytes.copyOf(minOf(16, bytes.size)))
        return mime to bytes
    }

    fun archivedThreadIds(): List<String> {
        val out = mutableListOf<String>()
        try {
            context.contentResolver.query(
                Telephony.Threads.CONTENT_URI,
                arrayOf(Telephony.Threads._ID),
                "archived=1",
                null,
                null,
            )?.use { cursor ->
                val id = cursor.getColumnIndex(Telephony.Threads._ID)
                if (id < 0) return@use
                while (cursor.moveToNext()) out += cursor.getLong(id).toString()
            }
        } catch (_: Exception) {
        }
        return out
    }

    fun setArchived(threadId: String, archived: Boolean) {
        val id = threadId.toLongOrNull() ?: return
        try {
            val values = ContentValues().apply { put("archived", if (archived) 1 else 0) }
            context.contentResolver.update(
                ContentUris.withAppendedId(Telephony.Threads.CONTENT_URI, id),
                values,
                null,
                null,
            )
            emitRefresh()
        } catch (_: Exception) {
        }
    }

    fun markUnread(threadId: String): Boolean {
        val id = threadId.toLongOrNull() ?: return false
        val hit = try {
            newestIncoming(id)
        } catch (_: Exception) {
            null
        } ?: return false
        val values = ContentValues().apply {
            put("read", 0)
            put("seen", 0)
        }
        val rows = try {
            context.contentResolver.update(hit, values, null, null)
        } catch (_: Exception) {
            0
        }
        if (rows > 0) emitRefresh()
        return rows > 0
    }

    fun blockedNumbers(): List<String> {
        val out = mutableListOf<String>()
        try {
            context.contentResolver.query(
                BlockedNumberContract.BlockedNumbers.CONTENT_URI,
                arrayOf(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER),
                null,
                null,
                null,
            )?.use { cursor ->
                val index = cursor.getColumnIndex(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
                if (index < 0) return@use
                while (cursor.moveToNext()) {
                    val number = cursor.getString(index).orEmpty()
                    if (number.isNotBlank()) out += number
                }
            }
        } catch (_: Exception) {
        }
        return out
    }

    fun blockNumber(number: String) {
        val clean = number.trim()
        if (clean.isEmpty()) return
        try {
            if (BlockedNumberContract.isBlocked(context, clean)) return
            val values = ContentValues().apply {
                put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, clean)
            }
            context.contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
        } catch (_: Exception) {
        }
    }

    fun unblockNumber(number: String) {
        val clean = number.trim()
        if (clean.isEmpty()) return
        try {
            context.contentResolver.delete(
                BlockedNumberContract.BlockedNumbers.CONTENT_URI,
                "${BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER}=?",
                arrayOf(clean),
            )
        } catch (_: Exception) {
        }
    }

    private fun newestIncoming(threadId: Long): Uri? {
        var smsUri: Uri? = null
        var smsAt = -1L
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID, Telephony.Sms.DATE),
            "${Telephony.Sms.THREAD_ID}=? AND ${Telephony.Sms.TYPE}=?",
            arrayOf(threadId.toString(), Telephony.Sms.MESSAGE_TYPE_INBOX.toString()),
            "${Telephony.Sms.DATE} DESC",
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                smsAt = cursor.getLong(1)
                smsUri = ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, cursor.getLong(0))
            }
        }
        var mmsUri: Uri? = null
        var mmsAt = -1L
        context.contentResolver.query(
            Telephony.Mms.CONTENT_URI,
            arrayOf(Telephony.Mms._ID, Telephony.Mms.DATE),
            "${Telephony.Mms.THREAD_ID}=? AND ${Telephony.Mms.MESSAGE_BOX}=?",
            arrayOf(threadId.toString(), Telephony.Mms.MESSAGE_BOX_INBOX.toString()),
            "${Telephony.Mms.DATE} DESC",
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val raw = cursor.getLong(1)
                mmsAt = if (raw <= 10_000_000_000L) raw * 1000 else raw
                mmsUri = ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, cursor.getLong(0))
            }
        }
        return when {
            smsUri == null -> mmsUri
            mmsUri == null -> smsUri
            mmsAt > smsAt -> mmsUri
            else -> smsUri
        }
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
                Telephony.Mms.MESSAGE_TYPE,
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
            val iType = cursor.getColumnIndex(Telephony.Mms.MESSAGE_TYPE)
            while (cursor.moveToNext()) {
                val threadId = cursor.getLong(iThread)
                val acc = threads.getOrPut(threadId) { ConversationAcc(threadId = threadId) }
                val date = mmsTimestampMillis(cursor.getLong(iDate))
                val box = cursor.getInt(iBox)
                val type = if (iType >= 0) cursor.getInt(iType) else -1
                if (box != Telephony.Mms.MESSAGE_BOX_DRAFTS && type != 130 && !acc.latestDecided) {
                    acc.latestDecided = true
                    acc.latestMms = latestMessageIsMms(
                        if (acc.sawLiveSms) acc.liveSmsAt else null,
                        date,
                    )
                }
                if (box != Telephony.Mms.MESSAGE_BOX_DRAFTS && date >= acc.timestamp) {
                    val preview = mmsPreview(cursor.getLong(iId))
                    if (!(type == 130 && preview == "Media message")) {
                        acc.timestamp = date
                        acc.snippet = preview
                    }
                }
                if (box == Telephony.Mms.MESSAGE_BOX_INBOX && cursor.getInt(iRead) == 0) {
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
            // DATE is seconds. Include a picture that shares the boundary second.
            arrayOf(threadId.toString(), (((before - 1) / 1000) + 1).toString())
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
                Telephony.Mms.SUBSCRIPTION_ID,
                Telephony.Mms.MESSAGE_TYPE,
            ),
            selection,
            args,
            "${Telephony.Mms.DATE} DESC",
        )?.use { cursor ->
            val iId = cursor.getColumnIndexOrThrow(Telephony.Mms._ID)
            val iThread = cursor.getColumnIndexOrThrow(Telephony.Mms.THREAD_ID)
            val iDate = cursor.getColumnIndexOrThrow(Telephony.Mms.DATE)
            val iBox = cursor.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_BOX)
            val iSub = cursor.getColumnIndex(Telephony.Mms.SUBSCRIPTION_ID)
            val iType = cursor.getColumnIndex(Telephony.Mms.MESSAGE_TYPE)
            while (cursor.moveToNext() && out.size < limit) {
                val mmsId = cursor.getLong(iId)
                val address = mmsAddress(mmsId).orEmpty()
                val box = cursor.getInt(iBox)
                if (box == Telephony.Mms.MESSAGE_BOX_DRAFTS) continue
                val parts = mmsParts(mmsId)
                val text = parts.firstOrNull { it.mimeType.startsWith("text/") }?.text.orEmpty()
                val kind = if (iType >= 0) cursor.getInt(iType) else -1
                if (text.isBlank() && parts.none {
                        it.mimeType.startsWith("image/") ||
                            it.mimeType.startsWith("video/") ||
                            it.mimeType.startsWith("audio/")
                    } && box == Telephony.Mms.MESSAGE_BOX_INBOX && kind == 130
                ) {
                    continue
                }
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
                    timestamp = mmsTimestampMillis(cursor.getLong(iDate)),
                    incoming = box == Telephony.Mms.MESSAGE_BOX_INBOX,
                    type = "mms",
                    status = if (box == Telephony.Mms.MESSAGE_BOX_SENT) "sent" else "received",
                    attachments = attachments,
                    simSlot = slotFor(if (iSub >= 0) cursor.getInt(iSub) else -1),
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
                val declared = cursor.getString(iCt).orEmpty()
                if (declared.equals("application/smil", true)) continue
                val partId = cursor.getLong(iId)
                val name = cursor.getString(iName)
                val mime = displayMime(declared, name, partHead(partId))
                val column = cursor.getString(iText)
                val text = if (mime.startsWith("text/")) {
                    partText(column, if (column.isNullOrBlank()) partBytes(partId, 16_000) else null)
                } else {
                    column
                }
                out += MmsPart(
                    id = partId.toString(),
                    mimeType = mime,
                    name = name,
                    text = text,
                )
            }
        }
        return out
    }

    private fun mmsPreview(mmsId: Long): String {
        val parts = mmsParts(mmsId)
        val text = parts.firstOrNull { it.mimeType.startsWith("text/") }?.text?.trim().orEmpty()
        if (text.isNotEmpty()) return text
        val mime = parts.firstOrNull {
            it.mimeType.startsWith("image/") || it.mimeType.startsWith("video/") || it.mimeType.startsWith("audio/")
        }?.mimeType.orEmpty()
        return when {
            mime.startsWith("image/") -> "Picture"
            mime.startsWith("video/") -> "Video"
            mime.startsWith("audio/") -> "Audio"
            else -> "Media message"
        }
    }

    private fun partHead(partId: Long): ByteArray? = partBytes(partId, 16)

    private fun partBytes(partId: Long, max: Int): ByteArray? {
        return try {
            context.contentResolver.openInputStream(Uri.parse("content://mms/part/$partId"))?.use { stream ->
                val buf = ByteArray(max)
                var n = 0
                while (n < max) {
                    val read = stream.read(buf, n, max - n)
                    if (read < 0) break
                    n += read
                }
                if (n == 0) null else buf.copyOf(n)
            }
        } catch (_: Exception) {
            null
        }
    }

    private val simSlots = HashMap<Int, Int>()

    private fun slotFor(subId: Int): Int {
        if (subId < 0) return -1
        synchronized(simSlots) {
            simSlots[subId]?.let { return it }
            val slot = try {
                context.getSystemService(SubscriptionManager::class.java)
                    ?.getActiveSubscriptionInfo(subId)
                    ?.simSlotIndex
                    ?: -1
            } catch (_: SecurityException) {
                -1
            }
            simSlots[subId] = slot
            return slot
        }
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
        var latestMms: Boolean = false,
        var sawLiveSms: Boolean = false,
        var liveSmsAt: Long = 0L,
        var latestDecided: Boolean = false,
        val ids: MutableList<Long> = mutableListOf(),
    ) {
        fun toDto(contacts: ContactsRepository, region: String): ConversationDto {
            val contact = contacts.lookup(address)
            val name = contact?.name ?: PhoneNumbers.display(address, region).ifBlank { address }
            return ConversationDto(
                id = threadId.toString(),
                address = address,
                displayName = name,
                photoUrl = contacts.photoUrlFor(address),
                snippet = snippet,
                timestamp = timestamp,
                unread = unread,
                isGroup = address.contains(","),
                recipients = address.split(",").map { it.trim() }.filter { it.isNotBlank() },
                avatarColor = contact?.color ?: ContactsRepository.avatarColor(name.ifBlank { address }),
                contactId = contact?.id,
                latestMms = latestMms,
            )
        }
    }
}

data class StoredMessageId(val mms: Boolean, val id: Long)

fun parseStoredMessageId(messageId: String): StoredMessageId? {
    val mms = when {
        messageId.startsWith("mms-") -> true
        messageId.startsWith("sms-") -> false
        else -> return null
    }
    val id = messageId.substring(4).toLongOrNull() ?: return null
    if (id <= 0L) return null
    return StoredMessageId(mms, id)
}

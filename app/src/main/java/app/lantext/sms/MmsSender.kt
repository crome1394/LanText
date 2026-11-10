package app.lantext.sms

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import com.google.android.mms.ContentType
import com.google.android.mms.InvalidHeaderValueException
import com.google.android.mms.pdu_alt.CharacterSets
import com.google.android.mms.pdu_alt.EncodedStringValue
import com.google.android.mms.pdu_alt.PduBody
import com.google.android.mms.pdu_alt.PduComposer
import com.google.android.mms.pdu_alt.PduHeaders
import com.google.android.mms.pdu_alt.PduPart
import com.google.android.mms.pdu_alt.SendReq
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object MmsSender {
    private const val TAG = "LanText.Mms"
    class Outbound(val bytes: ByteArray, val mime: String)
    data class ImagePayload(val bytes: ByteArray, val mime: String)
    data class Prepared(
        val bytes: ByteArray,
        val contentType: String,
        val fileName: String,
        val smilTag: String,
    )

    fun prepare(bytes: ByteArray, mime: String, maxBytes: Int, maxEdge: Int): Prepared {
        val kind = mime.lowercase()
        val budget = maxBytes.coerceIn(20_000, 1_200_000)
        return when {
            kind.contains("gif") -> {
                val cap = minOf(GifSearch.MAX_BYTES, budget)
                require(bytes.size <= cap) { "GIF is too large for this carrier." }
                require(bytes.size >= 6 && bytes[0] == 'G'.code.toByte()) { "Not a GIF" }
                Prepared(bytes, ContentType.IMAGE_GIF, "image_0.gif", "img")
            }
            kind.startsWith("audio/") -> {
                val amr = MmsAudio.toAmr(bytes)
                require(amr.size <= budget) { "That audio clip is too large for this carrier." }
                Prepared(amr, ContentType.AUDIO_AMR, "audio_0.amr", "audio")
            }
            kind.startsWith("video/") -> {
                require(bytes.size <= budget) { "That video is too large for this carrier." }
                if (kind.contains("3gpp")) {
                    Prepared(bytes, ContentType.VIDEO_3GPP, "video_0.3gp", "video")
                } else if (kind.contains("mp4")) {
                    Prepared(bytes, ContentType.VIDEO_MP4, "video_0.mp4", "video")
                } else {
                    throw IllegalArgumentException("That video format cannot be sent. Choose an MP4.")
                }
            }
            else -> {
                val img = resizeIfNeeded(bytes, budget, maxEdge)
                Prepared(img.bytes, ContentType.IMAGE_JPEG, "image_0.jpg", "img")
            }
        }
    }

    fun resizeIfNeeded(bytes: ByteArray, maxBytes: Int = 200_000, maxEdge: Int = 1024): ImagePayload {
        val edgeLimit = maxEdge.coerceIn(160, 2048)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val sampled = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, edgeLimit)
        }
        val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, sampled)
            ?: throw IllegalArgumentException("Could not read that picture")
        var edge = edgeLimit
        var current = scaleDown(original, edge)
        if (current !== original) original.recycle()
        var quality = 80
        try {
            while (true) {
                val stream = ByteArrayOutputStream()
                if (!current.compress(Bitmap.CompressFormat.JPEG, quality, stream)) {
                    throw IllegalArgumentException("Could not read that picture")
                }
                val out = stream.toByteArray()
                if (out.size <= maxBytes) return ImagePayload(out, "image/jpeg")
                if (quality > 45) {
                    quality -= 10
                    continue
                }
                val nextEdge = (edge * 3) / 4
                if (nextEdge < 320 || nextEdge >= edge) {
                    throw IllegalArgumentException("That picture is too large for this carrier.")
                }
                edge = nextEdge
                val scaled = scaleDown(current, edge)
                if (scaled !== current) current.recycle()
                current = scaled
                quality = 70
            }
        } finally {
            if (!current.isRecycled) current.recycle()
        }
    }

    private fun scaleDown(source: Bitmap, maxEdge: Int): Bitmap {
        val largest = maxOf(source.width, source.height)
        if (largest <= maxEdge || largest <= 0) return source
        val ratio = maxEdge.toFloat() / largest
        val width = (source.width * ratio).toInt().coerceAtLeast(1)
        val height = (source.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    private fun sampleSize(width: Int, height: Int, maxEdge: Int): Int {
        var sample = 1
        val longEdge = maxOf(width, height)
        if (longEdge <= 0 || maxEdge <= 0) return 1
        while (longEdge / (sample * 2) >= maxEdge) sample *= 2
        return sample
    }

    fun send(
        context: Context,
        manager: SmsManager,
        recipients: List<String>,
        text: String,
        image: ByteArray,
        mime: String,
        subscriptionId: Int,
        deliveryReport: Boolean = false,
    ): Outbound {
        val limits = carrierLimits(manager)
        val media = prepare(image, mime, limits.first, limits.second)
        val wire = recipients.map { mmsWireAddress(context, it) }.filter { it.isNotEmpty() }
        require(wire.isNotEmpty()) { "Add a recipient." }
        val req = SendReq()
        // SendReq already sets From to insert-address-token. The carrier writes
        // the subscriber number. A handset number here is how a picture to
        // yourself gets accepted and then never delivered back.
        for (to in wire) {
            req.addTo(EncodedStringValue(to))
        }
        req.setDate(System.currentTimeMillis() / 1000)
        val body = PduBody()
        val mediaPart = PduPart()
        mediaPart.setContentType(media.contentType.toByteArray())
        mediaPart.setContentLocation(media.fileName.toByteArray())
        mediaPart.setContentId(media.fileName.toByteArray())
        mediaPart.setName(media.fileName.toByteArray())
        mediaPart.setData(media.bytes)
        body.addPart(mediaPart)
        val textBytes = text.toByteArray(Charsets.UTF_8)
        if (text.isNotBlank()) {
            val textPart = PduPart()
            textPart.setCharset(CharacterSets.UTF_8)
            textPart.setContentType(ContentType.TEXT_PLAIN.toByteArray())
            textPart.setContentLocation("text_0.txt".toByteArray())
            textPart.setContentId("text_0.txt".toByteArray())
            textPart.setName("text_0.txt".toByteArray())
            textPart.setData(textBytes)
            body.addPart(textPart)
        }
        val smilBytes = smilXml(text.isNotBlank(), media).toByteArray(Charsets.UTF_8)
        val smilPart = PduPart()
        smilPart.setContentId("<smil>".toByteArray())
        smilPart.setContentLocation("smil.xml".toByteArray())
        smilPart.setContentType(ContentType.APP_SMIL.toByteArray())
        smilPart.setName("smil.xml".toByteArray())
        smilPart.setData(smilBytes)
        body.addPart(0, smilPart)
        req.setBody(body)
        req.setMessageSize((media.bytes.size + textBytes.size + smilBytes.size).toLong())
        req.setMessageClass(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.toByteArray())
        req.setExpiry(7 * 24 * 60 * 60)
        try {
            req.setPriority(PduHeaders.PRIORITY_NORMAL)
            req.setDeliveryReport(if (deliveryReport) PduHeaders.VALUE_YES else PduHeaders.VALUE_NO)
            req.setReadReport(PduHeaders.VALUE_NO)
        } catch (_: InvalidHeaderValueException) {
        }

        val pdu = PduComposer(context, req).make()
            ?: throw IllegalStateException("Could not build the MMS")
        val fileName = "send." + UUID.randomUUID() + ".dat"
        File(context.cacheDir, fileName).writeBytes(pdu)
        val contentUri = Uri.Builder()
            .authority(context.packageName + ".MmsFileProvider")
            .path(fileName)
            .scheme("content")
            .build()
        val grantTo = listOf("com.android.mms.service", "com.android.phone", "com.android.providers.telephony")
        for (pkg in grantTo) {
            try {
                context.grantUriPermission(pkg, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
            }
        }

        val waiter = MmsSentReceiver.register(fileName)
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        flags = if (Build.VERSION.SDK_INT >= 31) flags or PendingIntent.FLAG_MUTABLE else flags
        val sent = PendingIntent.getBroadcast(
            context,
            fileName.hashCode(),
            Intent(context, MmsSentReceiver::class.java).putExtra(MmsSentReceiver.EXTRA_ID, fileName),
            flags,
        )
        // Null overrides keep the carrier MMSC, proxy, port, and user agent.
        Log.i(TAG, "sending MMS pdu=${pdu.size} recipients=${wire.size} sub=$subscriptionId")
        manager.sendMultimediaMessage(context, contentUri, null, null, sent)
        val finished = waiter.latch.await(30, TimeUnit.SECONDS)
        val code = waiter.code.get()
        Log.i(TAG, "mms finished=$finished code=$code")
        if (!finished) {
            throw IllegalStateException("The MMS is still sending. If Fossify says it failed, turn on mobile data and retry there.")
        }
        if (code != Activity.RESULT_OK) {
            throw IllegalStateException(mmsErrorMessage(code))
        }
        return Outbound(media.bytes, media.contentType)
    }

    fun mediaBudget(manager: SmsManager): Int = carrierLimits(manager).first

    private fun carrierLimits(manager: SmsManager): Pair<Int, Int> {
        var maxBytes = 300_000
        var maxWidth = 1024
        var maxHeight = 1024
        if (Build.VERSION.SDK_INT >= 28) {
            val config = try {
                manager.carrierConfigValues
            } catch (_: Throwable) {
                null
            }
            if (config != null) {
                maxBytes = config.getInt(SmsManager.MMS_CONFIG_MAX_MESSAGE_SIZE, maxBytes)
                maxWidth = config.getInt(SmsManager.MMS_CONFIG_MAX_IMAGE_WIDTH, maxWidth)
                maxHeight = config.getInt(SmsManager.MMS_CONFIG_MAX_IMAGE_HEIGHT, maxHeight)
            }
        }
        if (maxBytes <= 0) maxBytes = 300_000
        val budget = (maxBytes - 16_384).coerceIn(20_000, 1_200_000)
        val edge = minOf(
            if (maxWidth > 0) maxWidth else 1024,
            if (maxHeight > 0) maxHeight else 1024,
        ).coerceIn(160, 2048)
        return budget to edge
    }

    private fun mmsErrorMessage(code: Int): String = when (code) {
        SmsManager.MMS_ERROR_NO_DATA_NETWORK,
        SmsManager.MMS_ERROR_DATA_DISABLED,
        -> "MMS needs mobile data, even on Wi-Fi. Turn mobile data on and try again."
        SmsManager.MMS_ERROR_INVALID_APN,
        SmsManager.MMS_ERROR_CONFIGURATION_ERROR,
        SmsManager.MMS_ERROR_UNABLE_CONNECT_MMS,
        SmsManager.MMS_ERROR_HTTP_FAILURE,
        -> "The carrier did not accept the MMS. Try a smaller GIF or a shorter voice note, or tap retry in Fossify Messages."
        SmsManager.MMS_ERROR_IO_ERROR -> "Could not send the MMS (the phone could not read it). Try again."
        else -> "Could not send the MMS (error $code). You can tap retry in Fossify Messages."
    }

    private fun smilXml(hasText: Boolean, media: Prepared): String {
        val textRegion = if (hasText) """<region id="Text" top="70%" height="30%" fit="scroll"/>""" else ""
        val textPar = if (hasText) """<text src="text_0.txt" region="Text"/>""" else ""
        val mediaTag = """<${media.smilTag} src="${media.fileName}" region="Image"/>"""
        val dur = when (media.smilTag) {
            "audio" -> "45000ms"
            "video" -> "30000ms"
            else -> "5000ms"
        }
        return """<smil><head><layout>
<root-layout width="320px" height="480px"/>
<region id="Image" top="0" left="0" height="${if (hasText) "70%" else "100%"}" fit="meet"/>
$textRegion
</layout></head><body><par dur="$dur">
$mediaTag$textPar
</par></body></smil>"""
    }
}

class MmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getStringExtra(EXTRA_ID) ?: return
        val waiter = waiters.remove(id) ?: return
        android.util.Log.i("LanText.Mms", "sent receiver code=$resultCode id=$id")
        waiter.code.set(resultCode)
        waiter.latch.countDown()
    }

    data class Waiter(
        val latch: CountDownLatch = CountDownLatch(1),
        val code: AtomicInteger = AtomicInteger(Int.MIN_VALUE),
    )

    companion object {
        const val EXTRA_ID = "mms_id"
        private val waiters = ConcurrentHashMap<String, Waiter>()
        fun register(id: String): Waiter {
            val w = Waiter()
            waiters[id] = w
            return w
        }
    }
}

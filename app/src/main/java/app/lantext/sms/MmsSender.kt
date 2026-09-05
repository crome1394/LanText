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
import android.os.Bundle
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
    data class ImagePayload(val bytes: ByteArray, val mime: String)
    data class Prepared(
        val bytes: ByteArray,
        val contentType: String,
        val fileName: String,
        val smilTag: String,
    )

    fun prepare(bytes: ByteArray, mime: String): Prepared {
        val kind = mime.lowercase()
        return when {
            kind.contains("gif") -> {
                require(bytes.size <= GifSearch.MAX_BYTES) {
                    "GIF is too large for MMS (carriers cap around 300 KB)"
                }
                require(bytes.size >= 6 && bytes[0] == 'G'.code.toByte()) { "Not a GIF" }
                Prepared(bytes, ContentType.IMAGE_GIF, "image_0.gif", "img")
            }
            kind.startsWith("audio/") -> {
                val amr = MmsAudio.toAmr(bytes)
                Prepared(amr, ContentType.AUDIO_AMR, "audio_0.amr", "audio")
            }
            else -> {
                val img = resizeIfNeeded(bytes, mime)
                Prepared(img.bytes, ContentType.IMAGE_JPEG, "image_0.jpg", "img")
            }
        }
    }

    fun resizeIfNeeded(bytes: ByteArray, mime: String, maxBytes: Int = 200_000, maxEdge: Int = 1024): ImagePayload {
        val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IllegalArgumentException("Could not read that picture")
        val scale = maxOf(original.width, original.height).toFloat() / maxEdge
        val bmp = if (scale > 1f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width / scale).toInt().coerceAtLeast(1),
                (original.height / scale).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            original
        }
        var quality = 80
        var out: ByteArray
        do {
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            out = stream.toByteArray()
            quality -= 10
        } while (out.size > maxBytes && quality >= 35)
        if (bmp !== original) bmp.recycle()
        if (!original.isRecycled) original.recycle()
        return ImagePayload(out, "image/jpeg")
    }

    fun send(
        context: Context,
        manager: SmsManager,
        recipients: List<String>,
        text: String,
        image: ByteArray,
        mime: String,
        subscriptionId: Int,
    ) {
        val media = prepare(image, mime)
        val req = SendReq()
        req.prepareFromAddress(context, "", subscriptionId)
        for (to in recipients) {
            req.addTo(EncodedStringValue(to))
        }
        req.setDate(System.currentTimeMillis() / 1000)
        val body = PduBody()
        val mediaPart = PduPart()
        mediaPart.setContentType(media.contentType.toByteArray())
        mediaPart.setContentLocation(media.fileName.toByteArray())
        mediaPart.setContentId("media".toByteArray())
        mediaPart.setName(media.fileName.toByteArray())
        mediaPart.setData(media.bytes)
        body.addPart(mediaPart)
        if (text.isNotBlank()) {
            val textPart = PduPart()
            textPart.setCharset(CharacterSets.UTF_8)
            textPart.setContentType(ContentType.TEXT_PLAIN.toByteArray())
            textPart.setContentLocation("text_0.txt".toByteArray())
            textPart.setContentId("text".toByteArray())
            textPart.setName("text_0.txt".toByteArray())
            textPart.setData(text.toByteArray(Charsets.UTF_8))
            body.addPart(textPart)
        }
        val smilPart = PduPart()
        smilPart.setContentId("smil".toByteArray())
        smilPart.setContentLocation("smil.xml".toByteArray())
        smilPart.setContentType(ContentType.APP_SMIL.toByteArray())
        smilPart.setData(smilXml(text.isNotBlank(), media).toByteArray(Charsets.UTF_8))
        body.addPart(0, smilPart)
        req.setBody(body)
        req.setMessageSize(media.bytes.size.toLong() + text.length)
        req.setMessageClass(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.toByteArray())
        req.setExpiry(7 * 24 * 60 * 60)
        try {
            req.setPriority(PduHeaders.PRIORITY_NORMAL)
            req.setDeliveryReport(PduHeaders.VALUE_NO)
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
        val overrides = Bundle()
        overrides.putBoolean(SmsManager.MMS_CONFIG_GROUP_MMS_ENABLED, false)
        Log.i(TAG, "sending MMS pdu=${pdu.size} uri=$contentUri to=$recipients")
        manager.sendMultimediaMessage(context, contentUri, null, overrides, sent)
        val finished = waiter.latch.await(30, TimeUnit.SECONDS)
        val code = waiter.code.get()
        Log.i(TAG, "mms finished=$finished code=$code")
        if (!finished) {
            throw IllegalStateException("The MMS is still sending. If Fossify says it failed, turn on mobile data and retry there.")
        }
        if (code != Activity.RESULT_OK) {
            throw IllegalStateException(mmsErrorMessage(code))
        }
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
        val dur = if (media.smilTag == "audio") "45000ms" else "5000ms"
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

package app.lantext.sms

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ThreadPdf {
    private const val PAGE_W = 612
    private const val PAGE_H = 792
    private const val MARGIN = 48f
    private const val MAX_MESSAGES = 1500

    fun fileName(displayName: String, now: Long = System.currentTimeMillis()): String {
        val who = displayName.trim().ifBlank { "thread" }
            .replace(Regex("[^A-Za-z0-9._-]+"), "-")
            .trim('-')
            .ifBlank { "thread" }
            .take(40)
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
        return "LanText-$who-$day.pdf"
    }

    fun render(
        title: String,
        subtitle: String,
        messages: List<MessageDto>,
        loadImage: (String) -> ByteArray?,
    ): ByteArray {
        val chronological = messages.sortedBy { it.timestamp }.takeLast(MAX_MESSAGES)
        val dateTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
        val dateOnly = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault())
        val doc = PdfDocument()
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF14201A.toInt()
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF5B6B63.toInt()
            textSize = 10f
        }
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF5B6B63.toInt()
            textSize = 9f
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF14201A.toInt()
            textSize = 11f
        }
        val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF5B6B63.toInt()
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val contentW = PAGE_W - MARGIN * 2
        val exported = "Exported ${dateTime.format(Date())}"
        var pageNum = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNum).create())
        var canvas = page.canvas
        var y = MARGIN

        fun newPage() {
            doc.finishPage(page)
            pageNum += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNum).create())
            canvas = page.canvas
            y = MARGIN
        }

        fun need(space: Float) {
            if (y + space > PAGE_H - MARGIN) newPage()
        }

        fun drawLines(lines: List<String>, paint: Paint) {
            val leading = paint.textSize + 4f
            for (line in lines) {
                need(leading)
                canvas.drawText(line, MARGIN, y + paint.textSize, paint)
                y += leading
            }
        }

        drawLines(wrap(title.ifBlank { "Conversation" }, titlePaint, contentW), titlePaint)
        if (subtitle.isNotBlank()) drawLines(wrap(subtitle, subPaint, contentW), subPaint)
        drawLines(listOf(exported), subPaint)
        y += 8f

        var lastDay = ""
        for (msg in chronological) {
            val day = dateOnly.format(Date(msg.timestamp))
            if (day != lastDay) {
                lastDay = day
                y += 8f
                drawLines(listOf(day), dayPaint)
            }
            val who = if (msg.incoming) msg.displayName.ifBlank { msg.address }.ifBlank { "Them" } else "You"
            val whenText = dateTime.format(Date(msg.timestamp))
            drawLines(wrap("$who  ·  $whenText", metaPaint, contentW), metaPaint)
            val body = msg.body.trim()
            if (body.isNotEmpty()) {
                drawLines(wrap(body, bodyPaint, contentW), bodyPaint)
            }
            for (att in msg.attachments) {
                if (!(att.mimeType.startsWith("image/"))) continue
                val id = att.url.substringAfterLast('/')
                val bytes = loadImage(id) ?: continue
                val bitmap = decodeScaled(bytes, contentW.toInt()) ?: continue
                val h = bitmap.height.toFloat() * (contentW / bitmap.width)
                val drawH = h.coerceAtMost(PAGE_H - MARGIN * 2)
                val drawW = contentW * (drawH / h)
                need(drawH + 8f)
                val dest = android.graphics.RectF(MARGIN, y, MARGIN + drawW, y + drawH)
                canvas.drawBitmap(bitmap, null, dest, null)
                y += drawH + 8f
                if (!bitmap.isRecycled) bitmap.recycle()
            }
            y += 6f
        }

        if (chronological.isEmpty()) {
            drawLines(listOf("No messages in this thread."), bodyPaint)
        }

        doc.finishPage(page)
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    internal fun wrap(text: String, paint: Paint, width: Float): List<String> {
        val lines = mutableListOf<String>()
        for (paragraph in text.replace("\r\n", "\n").split('\n')) {
            if (paragraph.isEmpty()) {
                lines += ""
                continue
            }
            var remaining = paragraph
            while (remaining.isNotEmpty()) {
                val count = paint.breakText(remaining, true, width, null)
                if (count <= 0) {
                    lines += remaining
                    break
                }
                var take = count
                if (take < remaining.length) {
                    val space = remaining.lastIndexOf(' ', take)
                    if (space > 0) take = space
                }
                lines += remaining.substring(0, take).trimEnd()
                remaining = remaining.substring(take).trimStart()
            }
        }
        return lines.ifEmpty { listOf("") }
    }

    private fun decodeScaled(bytes: ByteArray, maxWidth: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0) return null
        var sample = 1
        while (bounds.outWidth / sample > maxWidth * 2) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: return null
        if (raw.width <= maxWidth) return raw
        val h = (raw.height * (maxWidth.toFloat() / raw.width)).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(raw, maxWidth, h, true)
        if (scaled !== raw) raw.recycle()
        return scaled
    }
}

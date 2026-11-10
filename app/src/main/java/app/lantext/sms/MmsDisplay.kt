package app.lantext.sms

/** Provider dates are seconds, except a few rows already stored in milliseconds. */
fun mmsTimestampMillis(raw: Long): Long = if (raw > 10_000_000_000L) raw else raw * 1000L

/**
 * Turns a carrier part into something the thread can draw.
 * Some pictures arrive as application/octet-stream with only a file name.
 */
fun displayMime(declared: String, name: String?, head: ByteArray? = null): String {
    val mime = declared.lowercase().substringBefore(';').trim()
    if (mime == "image/jpg" || mime == "image/pjpeg") return "image/jpeg"
    if (mime.startsWith("image/") || mime.startsWith("video/") || mime.startsWith("audio/") || mime.startsWith("text/")) {
        return mime
    }
    sniffMime(head)?.let { return it }
    val ext = name.orEmpty().substringAfterLast('.', "").lowercase()
    return when (ext) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "mp4" -> "video/mp4"
        "3gp", "3gpp" -> "video/3gpp"
        "amr" -> "audio/amr"
        "txt" -> "text/plain"
        else -> mime
    }
}

fun imageContentType(bytes: ByteArray): String = sniffMime(bytes) ?: "image/jpeg"

fun partText(column: String?, fileBytes: ByteArray?): String? {
    val written = column?.trim().orEmpty()
    if (written.isNotEmpty()) return written
    if (fileBytes == null || fileBytes.isEmpty()) return null
    val text = fileBytes.toString(Charsets.UTF_8).trim().trim('\u0000')
    if (text.isEmpty() || text.any { ch ->
            ch == '\uFFFD' || ch.code == 0 || (ch.code < 32 && ch != '\n' && ch != '\r' && ch != '\t')
        }
    ) {
        return null
    }
    return text
}

private fun sniffMime(head: ByteArray?): String? {
    if (head == null || head.size < 3) return null
    if (head[0] == 0xFF.toByte() && head[1] == 0xD8.toByte()) return "image/jpeg"
    if (head.size >= 4 && head[0] == 0x89.toByte() && head[1] == 0x50.toByte() && head[2] == 0x4E.toByte()) return "image/png"
    if (head.size >= 6 && head[0] == 'G'.code.toByte() && head[1] == 'I'.code.toByte() && head[2] == 'F'.code.toByte()) return "image/gif"
    if (head.size >= 12 &&
        head[0] == 'R'.code.toByte() && head[1] == 'I'.code.toByte() && head[2] == 'F'.code.toByte() &&
        head[8] == 'W'.code.toByte() && head[9] == 'E'.code.toByte()
    ) {
        return "image/webp"
    }
    if (head.size >= 12 && head[4] == 'f'.code.toByte() && head[5] == 't'.code.toByte() && head[6] == 'y'.code.toByte() && head[7] == 'p'.code.toByte()) {
        return "video/mp4"
    }
    return null
}

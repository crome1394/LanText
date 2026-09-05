package app.lantext.sms

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GifSearch {
    const val MAX_BYTES = 280_000
    private const val MIN_BYTES = 2_048
    private val allowedHosts = setOf("upload.wikimedia.org")

    fun search(query: String): List<GifHit> {
        val q = query.trim().ifBlank { "reaction" }
        val url = "https://api.openverse.org/v1/images/" +
            "?q=${URLEncoder.encode(q, "UTF-8")}" +
            "&extension=gif&page_size=40&mature=false"
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("User-Agent", "LanText/0.2 (https://github.com/crome1394/LanText)")
            setRequestProperty("Accept", "application/json")
        }
        val raw = try {
            conn.inputStream.use { it.readBytes() }
        } finally {
            conn.disconnect()
        }
        val arr = JSONObject(String(raw, Charsets.UTF_8)).optJSONArray("results") ?: return emptyList()
        val out = ArrayList<GifHit>(24)
        for (i in 0 until arr.length()) {
            if (out.size >= 24) break
            val o = arr.optJSONObject(i) ?: continue
            val gifUrl = o.optString("url")
            val size = o.optInt("filesize")
            if (size !in MIN_BYTES..MAX_BYTES) continue
            if (!isAllowed(gifUrl)) continue
            out += GifHit(
                id = o.optString("id"),
                title = o.optString("title").ifBlank { q },
                url = gifUrl,
                width = o.optInt("width"),
                height = o.optInt("height"),
                bytes = size,
            )
        }
        return out
    }

    fun download(url: String): ByteArray {
        require(isAllowed(url)) { "That GIF source is not allowed" }
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "LanText/0.2 (https://github.com/crome1394/LanText)")
        }
        val bytes = try {
            conn.inputStream.use { it.readBytes() }
        } finally {
            conn.disconnect()
        }
        require(bytes.size in MIN_BYTES..MAX_BYTES) {
            "GIF is too large for MMS (carriers cap around 300 KB)"
        }
        require(bytes.size >= 6 && bytes[0] == 'G'.code.toByte() && bytes[1] == 'I'.code.toByte()) {
            "That file was not a GIF"
        }
        return bytes
    }

    fun isAllowed(url: String): Boolean {
        if (!url.startsWith("https://", ignoreCase = true)) return false
        val host = url.substringAfter("://").substringBefore("/").substringBefore(":")
        return host in allowedHosts
    }
}

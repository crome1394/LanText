package app.lantext.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object Crypto {
    private val random = SecureRandom()

    fun randomBytes(size: Int): ByteArray {
        val out = ByteArray(size)
        random.nextBytes(out)
        return out
    }

    fun randomToken(): String = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(32))

    fun randomId(): String = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(16))

    fun randomPin(): String {
        val n = random.nextInt(100_000_000)
        return String.format("%08d", n)
    }

    fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(data)
        return digest.joinToString("") { b -> "%02x".format(b) }
    }

    fun sha256Hex(text: String): String = sha256Hex(text.toByteArray(Charsets.UTF_8))
}

package app.lantext.net

import android.content.Context
import app.lantext.util.Crypto
import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.extensions.HashAlgorithm
import io.ktor.network.tls.extensions.SignatureAlgorithm
import java.io.File
import java.net.InetAddress
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

class TlsCertificateStore(context: Context) {
    private val dir = File(context.filesDir, "tls").apply { mkdirs() }
    private val storeFile = File(dir, "lantext.p12")
    val password: CharArray = "lantext-local".toCharArray()

    @Volatile private var cached: KeyStore? = null
    @Volatile var fingerprintSha256: String = ""
        private set

    fun keyStore(bindIp: String?): KeyStore {
        val existing = load()
        if (existing != null && fingerprintSha256.isNotBlank()) {
            return existing
        }
        return generate(bindIp)
    }

    fun regenerate(bindIp: String?): KeyStore {
        cached = null
        if (storeFile.exists()) storeFile.delete()
        return generate(bindIp)
    }

    private fun load(): KeyStore? {
        cached?.let { return it }
        if (!storeFile.exists()) return null
        return try {
            val ks = KeyStore.getInstance("PKCS12")
            storeFile.inputStream().use { ks.load(it, password) }
            cached = ks
            fingerprintSha256 = fingerprintOf(ks)
            ks
        } catch (_: Exception) {
            storeFile.delete()
            null
        }
    }

    private fun generate(bindIp: String?): KeyStore {
        val ips = listOfNotNull("127.0.0.1", bindIp)
        val ks = buildKeyStore {
            certificate(ALIAS) {
                hash = HashAlgorithm.SHA256
                sign = SignatureAlgorithm.ECDSA
                keySizeInBits = 256
                password = String(this@TlsCertificateStore.password)
                daysValid = 825
                subject = X500Principal("CN=LanText")
                ipAddresses = ips.map { InetAddress.getByName(it) }
                domains = listOf("lantext.local", "localhost")
            }
        }
        storeFile.outputStream().use { ks.store(it, password) }
        cached = ks
        fingerprintSha256 = fingerprintOf(ks)
        return ks
    }

    private fun fingerprintOf(ks: KeyStore): String {
        val cert = ks.getCertificate(ALIAS) as X509Certificate
        return Crypto.sha256Hex(cert.encoded)
    }

    companion object {
        const val ALIAS = "lantext"
    }
}

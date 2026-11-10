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
        if (existing != null && fingerprintSha256.isNotBlank() && coversDns(existing, LanDns.HOST)) {
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
            // ktor writes KeyStore.getDefaultType(). On Android that is BKS, not PKCS12.
            // Reading it as PKCS12 threw, deleted the file, and minted a new certificate
            // on every process start. Browsers that had already accepted the old one
            // then failed with no certificate page.
            val ks = KeyStore.getInstance(KeyStore.getDefaultType())
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
                subject = X500Principal(certificateSubject())
                ipAddresses = ips.map { InetAddress.getByName(it) }
                domains = listOf(LanDns.HOST, "localhost")
            }
        }
        storeFile.outputStream().use { ks.store(it, password) }
        cached = ks
        fingerprintSha256 = fingerprintOf(ks)
        return ks
    }

    private fun certificateSubject(): String {
        val raw = runCatching { app.lantext.LanTextApp.instance.productLabel.trim() }
            .getOrDefault("")
            .replace("\"", "")
            .ifBlank { "LanText" }
        return "CN=\"$raw\""
    }

    private fun coversDns(ks: KeyStore, host: String): Boolean {
        val cert = ks.getCertificate(ALIAS) as? X509Certificate ?: return false
        val names = try {
            dnsNamesFromSan(cert.subjectAlternativeNames)
        } catch (_: Exception) {
            emptyList()
        }
        // Replacing a certificate the browser already accepted makes fetch()
        // fail with no warning page. Keep a certificate whose names we cannot read.
        if (names.isEmpty()) return true
        val want = host.trim().trimEnd('.')
        return names.any { it.equals(want, true) }
    }

    private fun fingerprintOf(ks: KeyStore): String {
        val cert = ks.getCertificate(ALIAS) as X509Certificate
        return Crypto.sha256Hex(cert.encoded)
    }

    companion object {
        const val ALIAS = "lantext"

        /** DNS names from a certificate SAN list. The type is a Java Integer, not a Kotlin Int. */
        fun dnsNamesFromSan(rows: Collection<*>?): List<String> {
            if (rows == null) return emptyList()
            return rows.mapNotNull { item ->
                val row = item as? List<*> ?: return@mapNotNull null
                val type = (row.getOrNull(0) as? Number)?.toInt() ?: return@mapNotNull null
                if (type != 2) return@mapNotNull null
                val text = when (val value = row.getOrNull(1)) {
                    is String -> value
                    is ByteArray -> value.toString(Charsets.UTF_8)
                    else -> return@mapNotNull null
                }
                text.trim().trimEnd('.').takeIf { it.isNotEmpty() }
            }
        }
    }
}

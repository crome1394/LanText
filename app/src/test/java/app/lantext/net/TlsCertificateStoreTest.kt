package app.lantext.net

import app.lantext.util.Crypto
import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.extensions.HashAlgorithm
import io.ktor.network.tls.extensions.SignatureAlgorithm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.net.ssl.KeyManagerFactory
import java.io.File
import java.net.InetAddress
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

class TlsCertificateStoreTest {
    @Test
    fun wrapsTheOldPasswordWithoutChangingTheCertificate() {
        val dir = File.createTempFile("tls-store", "").apply {
            delete()
            mkdirs()
        }
        val legacyFile = File(dir, "lantext.p12")
        val created = buildKeyStore {
            certificate(TlsCertificateStore.ALIAS) {
                hash = HashAlgorithm.SHA256
                sign = SignatureAlgorithm.ECDSA
                keySizeInBits = 256
                password = String(TlsCertificateStore.LEGACY_STORE_PASSWORD)
                daysValid = 30
                subject = X500Principal("CN=\"LanText\"")
                domains = listOf(LanDns.HOST, "localhost")
                ipAddresses = listOf(InetAddress.getByName("127.0.0.1"))
            }
        }
        legacyFile.outputStream().use { created.store(it, TlsCertificateStore.LEGACY_STORE_PASSWORD) }
        val before = fingerprint(created)
        val box = MemoryPasswordBox()

        val first = TlsCertificateStore(dir, box).keyStore("127.0.0.1")
        assertNotNull(first)
        assertEquals(before, fingerprint(first!!))
        assertFalse(box.saved.contentEquals(TlsCertificateStore.LEGACY_STORE_PASSWORD))
        assertFalse(opensWith(legacyFile, TlsCertificateStore.LEGACY_STORE_PASSWORD))

        val secondStore = TlsCertificateStore(dir, box)
        val second = secondStore.keyStore("127.0.0.1")
        assertNotNull(second)
        assertEquals(before, fingerprint(second!!))
        assertTrue(second.getKey(TlsCertificateStore.ALIAS, secondStore.password) != null)
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(second, secondStore.password)
        assertTrue(kmf.keyManagers.isNotEmpty())
        dir.deleteRecursively()
    }

    private fun fingerprint(ks: KeyStore): String {
        val cert = ks.getCertificate(TlsCertificateStore.ALIAS) as X509Certificate
        return Crypto.sha256Hex(cert.encoded)
    }

    private fun opensWith(file: File, password: CharArray): Boolean {
        return try {
            val ks = KeyStore.getInstance(KeyStore.getDefaultType())
            file.inputStream().use { ks.load(it, password) }
            true
        } catch (_: Exception) {
            false
        }
    }
}

private class MemoryPasswordBox : StorePasswordBox {
    var saved: CharArray = CharArray(0)
    override fun read(): CharArray? = saved.takeIf { it.isNotEmpty() }?.copyOf()
    override fun write(password: CharArray) {
        saved = password.copyOf()
    }
}

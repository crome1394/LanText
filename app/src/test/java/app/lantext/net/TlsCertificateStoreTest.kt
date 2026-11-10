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
    fun keepsTheCertificateAcrossARestart() {
        val dir = File.createTempFile("tls-store", "").apply {
            delete()
            mkdirs()
        }
        val box = MemoryPasswordBox()
        val created = TlsCertificateStore(dir, box).keyStore("127.0.0.1")
        assertNotNull(created)
        val before = fingerprint(created!!)

        val again = TlsCertificateStore(dir, box)
        val reopened = again.keyStore("127.0.0.1")
        assertNotNull(reopened)
        assertEquals(before, fingerprint(reopened!!))
        assertTrue(reopened.getKey(TlsCertificateStore.ALIAS, again.password) != null)
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(reopened, again.password)
        assertTrue(kmf.keyManagers.isNotEmpty())
        dir.deleteRecursively()
    }

    @Test
    fun convertsAStoreSavedWithTheOldPassword() {
        val dir = tempDir()
        val file = File(dir, "lantext.p12")
        val created = sampleStore(String(TlsCertificateStore.LEGACY_STORE_PASSWORD))
        file.outputStream().use { created.store(it, TlsCertificateStore.LEGACY_STORE_PASSWORD) }
        val before = fingerprint(created)
        val box = MemoryPasswordBox()

        val first = TlsCertificateStore(dir, box).keyStore("127.0.0.1")
        assertNotNull(first)
        assertEquals(before, fingerprint(first!!))
        assertFalse(box.saved.contentEquals(TlsCertificateStore.LEGACY_STORE_PASSWORD))
        assertFalse(opensWith(file, TlsCertificateStore.LEGACY_STORE_PASSWORD))

        val second = TlsCertificateStore(dir, box).keyStore("127.0.0.1")
        assertEquals(before, fingerprint(second!!))
        dir.deleteRecursively()
    }

    @Test
    fun replacesACertificateThatCannotBeOpened() {
        val dir = tempDir()
        val file = File(dir, "lantext.p12")
        val created = sampleStore("fixture-only")
        file.outputStream().use { created.store(it, "fixture-only".toCharArray()) }
        val before = fingerprint(created)
        val replaced = TlsCertificateStore(dir, MemoryPasswordBox()).keyStore("127.0.0.1")
        assertNotNull(replaced)
        assertFalse(before == fingerprint(replaced!!))
        assertTrue(file.isFile)
        dir.deleteRecursively()
    }

    private fun tempDir(): File = File.createTempFile("tls-store", "").apply {
        delete()
        mkdirs()
    }

    private fun sampleStore(storePassword: String): KeyStore = buildKeyStore {
        certificate(TlsCertificateStore.ALIAS) {
            hash = HashAlgorithm.SHA256
            sign = SignatureAlgorithm.ECDSA
            keySizeInBits = 256
            password = storePassword
            daysValid = 30
            subject = X500Principal("CN=\"LanText\"")
            domains = listOf(LanDns.HOST, "localhost")
            ipAddresses = listOf(InetAddress.getByName("127.0.0.1"))
        }
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

    private fun fingerprint(ks: KeyStore): String {
        val cert = ks.getCertificate(TlsCertificateStore.ALIAS) as X509Certificate
        return Crypto.sha256Hex(cert.encoded)
    }

}

private class MemoryPasswordBox : StorePasswordBox {
    var saved: CharArray = CharArray(0)
    override fun read(): CharArray? = saved.takeIf { it.isNotEmpty() }?.copyOf()
    override fun write(password: CharArray) {
        saved = password.copyOf()
    }
}

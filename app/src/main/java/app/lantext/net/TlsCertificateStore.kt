package app.lantext.net

import android.content.Context
import android.util.Log
import app.lantext.util.Crypto
import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.extensions.HashAlgorithm
import io.ktor.network.tls.extensions.SignatureAlgorithm
import java.io.File
import java.net.InetAddress
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

class TlsCertificateStore internal constructor(
    private val dir: File,
    private val passwords: StorePasswordBox,
) {
    constructor(context: Context) : this(
        File(context.filesDir, "tls").apply { mkdirs() },
        AndroidKeystorePasswordBox(File(context.filesDir, "tls/store-secret.bin")),
    )

    private val storeFile = File(dir, "lantext.p12")

    @Volatile var password: CharArray = CharArray(0)
        private set

    @Volatile private var cached: KeyStore? = null
    @Volatile var fingerprintSha256: String = ""
        private set

    fun keyStore(bindIp: String?): KeyStore? {
        val existing = load()
        if (existing != null) {
            if (!coversDns(existing, LanDns.HOST)) {
                Log.w(TAG, "Keeping the existing computer inbox certificate")
            }
            return existing
        }
        if (storeFile.exists()) {
            Log.e(TAG, "Computer inbox certificate could not be opened")
            return null
        }
        return generate(bindIp)
    }

    fun regenerate(bindIp: String?): KeyStore {
        cached = null
        storeFile.delete()
        File(dir, storeFile.name + ".new").delete()
        File(dir, "store-secret.bin").delete()
        File(dir, "store-secret.bin.new").delete()
        return generate(bindIp) ?: error("Computer inbox certificate could not be created")
    }

    private fun load(): KeyStore? {
        cached?.let { return it }
        if (!storeFile.exists()) return null
        val wrapped = passwords.read()
        if (wrapped != null) {
            open(storeFile, wrapped)?.let { ks ->
                if (keyUnlocks(ks, wrapped)) return accept(ks, wrapped)
                // The file was re-saved, but the key inside it was still under the old password.
                if (keyUnlocks(ks, LEGACY_STORE_PASSWORD)) return reprotect(ks, LEGACY_STORE_PASSWORD)
            }
        }
        // A certificate saved before the password was wrapped. Re-save it once.
        open(storeFile, LEGACY_STORE_PASSWORD)?.let { ks ->
            if (keyUnlocks(ks, LEGACY_STORE_PASSWORD)) return reprotect(ks, LEGACY_STORE_PASSWORD)
        }
        return null
    }

    private fun reprotect(ks: KeyStore, currentPassword: CharArray): KeyStore {
        val next = randomPassword()
        val pending = File(dir, storeFile.name + ".new")
        return try {
            rekey(ks, currentPassword, next)
            pending.outputStream().use { ks.store(it, next) }
            val staged = open(pending, next)
            if (staged == null || !keyUnlocks(staged, next)) {
                pending.delete()
                rekey(ks, next, currentPassword)
                Log.e(TAG, "Could not wrap the computer inbox certificate password")
                return accept(ks, currentPassword)
            }
            passwords.write(next)
            replace(pending, storeFile)
            val reloaded = open(storeFile, next)
            if (reloaded == null || !keyUnlocks(reloaded, next)) {
                Log.e(TAG, "Could not reopen the computer inbox certificate")
                return accept(ks, next)
            }
            accept(reloaded, next)
        } catch (t: Exception) {
            pending.delete()
            Log.e(TAG, "Could not wrap the computer inbox certificate password", t)
            if (keyUnlocks(ks, next)) accept(ks, next) else accept(ks, currentPassword)
        }
    }

    private fun rekey(ks: KeyStore, currentPassword: CharArray, next: CharArray) {
        val key = ks.getKey(ALIAS, currentPassword) as java.security.PrivateKey
        ks.setKeyEntry(ALIAS, key, next, ks.getCertificateChain(ALIAS))
    }

    private fun keyUnlocks(ks: KeyStore, pass: CharArray): Boolean {
        return try {
            ks.getKey(ALIAS, pass) != null
        } catch (_: Exception) {
            false
        }
    }

    private fun generate(bindIp: String?): KeyStore? {
        val next = randomPassword()
        val ips = listOfNotNull("127.0.0.1", bindIp)
        val ks = buildKeyStore {
            certificate(ALIAS) {
                hash = HashAlgorithm.SHA256
                sign = SignatureAlgorithm.ECDSA
                keySizeInBits = 256
                password = String(next)
                daysValid = 825
                subject = X500Principal(certificateSubject())
                ipAddresses = ips.map { InetAddress.getByName(it) }
                domains = listOf(LanDns.HOST, "localhost")
            }
        }
        val pending = File(dir, storeFile.name + ".new")
        return try {
            dir.mkdirs()
            pending.outputStream().use { ks.store(it, next) }
            passwords.write(next)
            replace(pending, storeFile)
            accept(open(storeFile, next) ?: ks, next)
        } catch (t: Exception) {
            pending.delete()
            Log.e(TAG, "Could not create the computer inbox certificate", t)
            null
        }
    }

    private fun open(file: File, pass: CharArray): KeyStore? {
        if (!file.isFile) return null
        return try {
            val ks = KeyStore.getInstance(KeyStore.getDefaultType())
            file.inputStream().use { ks.load(it, pass) }
            ks
        } catch (_: Exception) {
            null
        }
    }

    private fun accept(ks: KeyStore, pass: CharArray): KeyStore {
        password = pass
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

    private fun randomPassword(): CharArray {
        return Crypto.randomBytes(32).joinToString("") { "%02x".format(it) }.toCharArray()
    }

    private fun replace(pending: File, target: File) {
        if (!pending.renameTo(target)) {
            pending.copyTo(target, overwrite = true)
            pending.delete()
        }
    }

    companion object {
        const val ALIAS = "lantext"
        private const val TAG = "LanText"

        /** Opens a store written when the password was still compiled into the app. */
        internal val LEGACY_STORE_PASSWORD: CharArray = "lantext-local".toCharArray()

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

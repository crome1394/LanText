package app.lantext.net

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Holds the certificate-store password outside the APK. */
internal interface StorePasswordBox {
    fun read(): CharArray?
    fun write(password: CharArray)
}

/**
 * Encrypts the certificate-store password with a key that never leaves
 * Android Keystore. The TLS certificate itself stays in the app file so a
 * browser that already accepted it keeps working.
 */
internal class AndroidKeystorePasswordBox(
    private val secretFile: File,
) : StorePasswordBox {
    override fun read(): CharArray? {
        decrypt(secretFile)?.let { return it }
        val pending = pendingFile()
        val recovered = decrypt(pending) ?: return null
        promote(pending)
        return recovered
    }

    override fun write(password: CharArray) {
        val pending = pendingFile()
        pending.parentFile?.mkdirs()
        pending.writeBytes(encrypt(password))
        promote(pending)
    }

    private fun pendingFile(): File = File(secretFile.parentFile, secretFile.name + ".new")

    private fun promote(pending: File) {
        if (!pending.isFile) return
        if (secretFile.exists()) secretFile.delete()
        if (!pending.renameTo(secretFile)) {
            pending.copyTo(secretFile, overwrite = true)
            pending.delete()
        }
    }

    private fun encrypt(password: CharArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey())
        val iv = cipher.iv
        val body = cipher.doFinal(String(password).toByteArray(Charsets.UTF_8))
        val out = ByteArray(1 + iv.size + body.size)
        out[0] = VERSION
        iv.copyInto(out, 1)
        body.copyInto(out, 1 + iv.size)
        return out
    }

    private fun decrypt(file: File): CharArray? {
        if (!file.isFile || file.length() < 1 + IV_LEN + TAG_LEN) return null
        val raw = file.readBytes()
        if (raw[0] != VERSION) return null
        val iv = raw.copyOfRange(1, 1 + IV_LEN)
        val body = raw.copyOfRange(1 + IV_LEN, raw.size)
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, masterKey(), GCMParameterSpec(128, iv))
            cipher.doFinal(body).toString(Charsets.UTF_8).toCharArray()
        } catch (_: Exception) {
            null
        }
    }

    private fun masterKey(): SecretKey = synchronized(AndroidKeystorePasswordBox::class.java) {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE)
        ks.load(null)
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        generator.init(spec)
        generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "lantext-tls-password"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val VERSION: Byte = 1
        const val IV_LEN = 12
        const val TAG_LEN = 16
    }
}

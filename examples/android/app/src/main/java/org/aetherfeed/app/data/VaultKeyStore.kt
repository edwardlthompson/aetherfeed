package org.aetherfeed.app.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class VaultKeyStore(private val context: Context) {
    fun passphrase(): ByteArray {
        org.aetherfeed.app.applock.AppLockHolder.vaultKey()?.let { return it }
        if (File(context.filesDir, WRAP_FILE).exists()) {
            error("vault locked")
        }
        return legacyPassphrase()
    }

    fun adoptAndClearLegacy(): ByteArray? {
        val file = File(context.noBackupFilesDir, KEY_FILE)
        if (!file.exists()) return null
        val raw = unwrap(file.readBytes())
        file.delete()
        return raw
    }

    fun legacyPassphrase(): ByteArray {
        val file = File(context.noBackupFilesDir, KEY_FILE)
        if (file.exists()) {
            return unwrap(file.readBytes())
        }
        val raw = ByteArray(KEY_BYTES).also { java.security.SecureRandom().nextBytes(it) }
        file.writeBytes(wrap(raw))
        return raw
    }

    private fun wrap(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        return cipher.iv + cipher.doFinal(plain)
    }

    private fun unwrap(blob: ByteArray): ByteArray {
        val iv = blob.copyOfRange(0, IV_BYTES)
        val packed = blob.copyOfRange(IV_BYTES, blob.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(packed)
    }

    private fun secretKey(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = store.getKey(ALIAS, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALIAS = "org.aetherfeed.app.vault"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_FILE = "vault.key"
        private const val WRAP_FILE = "lock-wrap.json"
        private const val KEY_BYTES = 32
        private const val IV_BYTES = 12
        private const val TAG_BITS = 128
    }
}

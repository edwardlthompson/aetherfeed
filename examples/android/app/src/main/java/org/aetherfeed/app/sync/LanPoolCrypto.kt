package org.aetherfeed.app.sync

import android.util.Base64
import com.google.crypto.tink.subtle.XChaCha20Poly1305
import org.aetherfeed.app.domain.SyncEnvelope
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** Argon2id + XChaCha20-Poly1305 SyncEnvelope helpers (match aetherfeed-crypto). */
object LanPoolCrypto {
    private const val SALT_LEN = 16
    private const val NONCE_LEN = 24
    private const val KEY_LEN = 32
    private const val MEMORY_KB = 19_456
    private const val ITERATIONS = 2
    private const val PARALLELISM = 1

    fun sealEnvelope(passphrase: String, plaintext: ByteArray): String {
        require(passphrase.isNotEmpty()) { "passphrase must not be empty" }
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(passphrase.toByteArray(Charsets.UTF_8), salt)
        val aead = XChaCha20Poly1305(key)
        // Tink prepends a random 24-byte nonce to ciphertext.
        val sealed = aead.encrypt(plaintext, byteArrayOf())
        require(sealed.size > NONCE_LEN) { "seal too short" }
        val nonce = sealed.copyOfRange(0, NONCE_LEN)
        val ciphertext = sealed.copyOfRange(NONCE_LEN, sealed.size)
        return JSONObject()
            .put("version", 1)
            .put("kdf", "argon2id")
            .put("aead", "xchacha20poly1305")
            .put("saltB64", b64(salt))
            .put("nonceB64", b64(nonce))
            .put("ciphertextB64", b64(ciphertext))
            .toString()
    }

    fun openEnvelope(passphrase: String, envelopeJson: String): ByteArray {
        require(passphrase.isNotEmpty()) { "passphrase must not be empty" }
        val obj = JSONObject(envelopeJson)
        val env =
            SyncEnvelope(
                version = obj.getInt("version"),
                kdf = obj.getString("kdf"),
                aead = obj.getString("aead"),
                saltB64 = obj.getString("saltB64"),
                nonceB64 = obj.getString("nonceB64"),
                ciphertextB64 = obj.getString("ciphertextB64"),
            )
        require(env.aead == "xchacha20poly1305") { "unsupported aead" }
        val salt = unb64(env.saltB64)
        val nonce = unb64(env.nonceB64)
        val ciphertext = unb64(env.ciphertextB64)
        val key = deriveKey(passphrase.toByteArray(Charsets.UTF_8), salt)
        val aead = XChaCha20Poly1305(key)
        val sealed = nonce + ciphertext
        return aead.decrypt(sealed, byteArrayOf())
    }

    fun putBearer(passphrase: String, authSaltB64: String, etag: String): String {
        require(passphrase.isNotEmpty()) { "passphrase must not be empty" }
        val key = deriveKey(passphrase.toByteArray(Charsets.UTF_8), unb64(authSaltB64))
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return b64(mac.doFinal("PUT\n$etag".toByteArray(Charsets.UTF_8)))
    }

    fun deriveKey(passphrase: ByteArray, salt: ByteArray): ByteArray {
        require(passphrase.isNotEmpty()) { "passphrase must not be empty" }
        val params =
            Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY_KB)
                .withParallelism(PARALLELISM)
                .withSalt(salt)
                .build()
        val gen = Argon2BytesGenerator()
        gen.init(params)
        val out = ByteArray(KEY_LEN)
        gen.generateBytes(passphrase, out)
        return out
    }

    private fun b64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun unb64(value: String): ByteArray = Base64.decode(value, Base64.DEFAULT)
}

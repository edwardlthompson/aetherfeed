package org.aetherfeed.app.sync

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

private const val ITERATIONS = 210_000

fun sealJson(plain: String, passphrase: String): ByteArray {
    require(passphrase.isNotBlank()) { "sync passphrase required" }
    val salt = randomBytes(16)
    val nonce = randomBytes(12)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, deriveKey(passphrase, salt), GCMParameterSpec(128, nonce))
    val ciphertext = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
    return JSONObject()
        .put("version", 1)
        .put("kdf", "pbkdf2")
        .put("aead", "aes-256-gcm")
        .put("saltB64", b64(salt))
        .put("nonceB64", b64(nonce))
        .put("ciphertextB64", b64(ciphertext))
        .toString()
        .toByteArray(Charsets.UTF_8)
}

fun openJson(blob: ByteArray, passphrase: String): String {
    require(passphrase.isNotBlank()) { "sync passphrase required" }
    val envelope = JSONObject(blob.toString(Charsets.UTF_8))
    require(envelope.optInt("version") == 1 && envelope.optString("aead") == "aes-256-gcm") {
        "unsupported envelope"
    }
    val salt = fromB64(envelope.getString("saltB64"))
    val nonce = fromB64(envelope.getString("nonceB64"))
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, deriveKey(passphrase, salt), GCMParameterSpec(128, nonce))
    return cipher.doFinal(fromB64(envelope.getString("ciphertextB64"))).toString(Charsets.UTF_8)
}

private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
    val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, 256)
    val raw = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    return SecretKeySpec(raw, "AES")
}

private fun randomBytes(size: Int): ByteArray = ByteArray(size).also { java.security.SecureRandom().nextBytes(it) }

private fun b64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

private fun fromB64(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

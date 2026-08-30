package org.aetherfeed.app.applock

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.aetherfeed.app.domain.AppLockSecretKind
import org.json.JSONObject

private const val ITERATIONS = 210_000

fun wrapBytes(plain: ByteArray, secret: String, kind: AppLockSecretKind? = null): ByteArray {
    require(secret.isNotBlank()) { "secret" }
    val salt = randomBytes(16)
    val nonce = randomBytes(12)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, deriveKey(secret, salt), GCMParameterSpec(128, nonce))
    val envelope = JSONObject()
        .put("version", 1)
        .put("kdf", "pbkdf2")
        .put("aead", "aes-256-gcm")
        .put("saltB64", b64(salt))
        .put("nonceB64", b64(nonce))
        .put("ciphertextB64", b64(cipher.doFinal(plain)))
    if (kind != null) envelope.put("kind", kind.name.lowercase())
    return envelope.toString().toByteArray(Charsets.UTF_8)
}

fun peekLockKind(blob: ByteArray): AppLockSecretKind? {
    val kind = runCatching { JSONObject(blob.toString(Charsets.UTF_8)).optString("kind") }.getOrNull()
    return when (kind) {
        "pin" -> AppLockSecretKind.Pin
        "passphrase" -> AppLockSecretKind.Passphrase
        else -> null
    }
}

fun openBytes(blob: ByteArray, secret: String): ByteArray {
    require(secret.isNotBlank()) { "secret" }
    val envelope = JSONObject(blob.toString(Charsets.UTF_8))
    require(envelope.optInt("version") == 1 && envelope.optString("aead") == "aes-256-gcm") {
        "unsupported envelope"
    }
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(
        Cipher.DECRYPT_MODE,
        deriveKey(secret, fromB64(envelope.getString("saltB64"))),
        GCMParameterSpec(128, fromB64(envelope.getString("nonceB64"))),
    )
    return cipher.doFinal(fromB64(envelope.getString("ciphertextB64")))
}

private fun deriveKey(secret: String, salt: ByteArray): SecretKeySpec {
    val spec = PBEKeySpec(secret.toCharArray(), salt, ITERATIONS, 256)
    val raw = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    return SecretKeySpec(raw, "AES")
}

private fun randomBytes(size: Int): ByteArray = ByteArray(size).also { java.security.SecureRandom().nextBytes(it) }

private fun b64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

private fun fromB64(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

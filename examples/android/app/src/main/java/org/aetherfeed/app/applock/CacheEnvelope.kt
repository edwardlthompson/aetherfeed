package org.aetherfeed.app.applock

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

private val MAGIC = byteArrayOf(0x41, 0x46, 0x33)

internal fun wrapCache(plain: ByteArray, key: ByteArray): ByteArray {
    require(key.size >= 32) { "vault key" }
    val nonce = ByteArray(12).also { java.security.SecureRandom().nextBytes(it) }
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, aes(key), GCMParameterSpec(128, nonce))
    val ct = cipher.doFinal(plain)
    return ByteArray(16 + ct.size).also { out ->
        MAGIC.copyInto(out)
        out[3] = 3
        nonce.copyInto(out, 4)
        ct.copyInto(out, 16)
    }
}

internal fun wrapCacheV2(plain: ByteArray, key: ByteArray): ByteArray {
    require(key.size >= 32) { "vault key" }
    val nonce = ByteArray(12).also { java.security.SecureRandom().nextBytes(it) }
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, aes(key), GCMParameterSpec(128, nonce))
    return JSONObject()
        .put("version", 2)
        .put("kdf", "raw")
        .put("aead", "aes-256-gcm")
        .put("nonceB64", Base64.encodeToString(nonce, Base64.NO_WRAP))
        .put("ciphertextB64", Base64.encodeToString(cipher.doFinal(plain), Base64.NO_WRAP))
        .toString()
        .toByteArray(Charsets.UTF_8)
}

internal fun openCache(blob: ByteArray, key: ByteArray): ByteArray {
    val version = cacheVersion(blob)
    if (version >= 3) {
        require(key.size >= 32 && blob.size > 16) { "vault key" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, aes(key), GCMParameterSpec(128, blob.copyOfRange(4, 16)))
        return cipher.doFinal(blob, 16, blob.size - 16)
    }
    if (version >= 2) {
        val env = JSONObject(blob.toString(Charsets.UTF_8))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            aes(key),
            GCMParameterSpec(128, Base64.decode(env.getString("nonceB64"), Base64.NO_WRAP)),
        )
        return cipher.doFinal(Base64.decode(env.getString("ciphertextB64"), Base64.NO_WRAP))
    }
    return openBytes(blob, Base64.encodeToString(key, Base64.NO_WRAP))
}

internal fun cacheVersion(blob: ByteArray): Int {
    if (blob.size >= 4 && blob[0] == MAGIC[0] && blob[1] == MAGIC[1] && blob[2] == MAGIC[2]) {
        return blob[3].toInt() and 0xFF
    }
    return runCatching { JSONObject(blob.toString(Charsets.UTF_8)).optInt("version", 1) }.getOrDefault(1)
}

private fun aes(key: ByteArray): SecretKeySpec = SecretKeySpec(key.copyOf(32), "AES")

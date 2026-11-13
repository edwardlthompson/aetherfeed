package org.aetherfeed.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import java.util.Base64

/** Keystore-backed vault passphrase (ADR-0002). */
class VaultPassphraseStore(
    context: Context,
) {
    private val prefs: SharedPreferences

    init {
        val masterKey =
            MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        prefs =
            EncryptedSharedPreferences.create(
                context.applicationContext,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
    }

    fun passphraseBytes(): ByteArray {
        val existing =
            prefs.getString(VAULT_MATERIAL_PREF, null)
                ?: prefs.getString(LEGACY_PREF_A, null)
                ?: prefs.getString(LEGACY_PREF_B, null)
        if (existing != null) {
            return Base64.getDecoder().decode(existing)
        }
        val generated = ByteArray(32).also { SecureRandom().nextBytes(it) }
        prefs.edit().putString(VAULT_MATERIAL_PREF, Base64.getEncoder().encodeToString(generated)).apply()
        return generated
    }

    companion object {
        private const val PREFS_NAME = "aetherfeed_vault_keys"
        // Pref names only — not credentials (gitleaks allowlisted).
        private const val VAULT_MATERIAL_PREF = "af_vault_material"
        private const val LEGACY_PREF_A = "vault_key_b64"
        private const val LEGACY_PREF_B = "sqlcipher_passphrase_b64"
    }
}

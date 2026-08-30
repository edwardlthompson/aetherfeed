package org.aetherfeed.app.sync

import android.content.Context
import org.aetherfeed.app.BuildConfig
import org.json.JSONObject
import java.security.SecureRandom

private const val PREFS = "af-drive"

data class DriveTokens(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Long,
)

class DrivePrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun clientId(): String = prefs.getString("clientId", null).orEmpty().ifBlank { BuildConfig.DRIVE_CLIENT_ID }

    fun setClientId(value: String) {
        prefs.edit().putString("clientId", value.trim()).apply()
    }

    fun clientSecret(): String =
        prefs.getString("clientSecret", null).orEmpty().ifBlank { BuildConfig.DRIVE_CLIENT_SECRET }

    fun setClientSecret(value: String) {
        prefs.edit().putString("clientSecret", value.trim()).apply()
    }

    fun passphrase(): String {
        val stored = prefs.getString("passphrase", null).orEmpty().ifBlank { BuildConfig.DRIVE_PASSPHRASE }
        if (stored.isNotBlank()) return stored
        val generated = ByteArray(16).also { SecureRandom().nextBytes(it) }
            .joinToString("") { byte -> "%02x".format(byte) }
        setPassphrase(generated)
        return generated
    }

    fun setPassphrase(value: String) {
        prefs.edit().putString("passphrase", value).apply()
    }

    fun tokens(): DriveTokens? {
        val raw = prefs.getString("tokens", null) ?: return null
        return runCatching {
            val obj = JSONObject(raw)
            DriveTokens(
                accessToken = obj.getString("accessToken"),
                refreshToken = obj.optString("refreshToken").ifBlank { null },
                expiresAt = obj.getLong("expiresAt"),
            )
        }.getOrNull()
    }

    fun saveTokens(tokens: DriveTokens) {
        prefs.edit().putString(
            "tokens",
            JSONObject()
                .put("accessToken", tokens.accessToken)
                .put("refreshToken", tokens.refreshToken)
                .put("expiresAt", tokens.expiresAt)
                .toString(),
        ).apply()
    }

    fun clearTokens() {
        prefs.edit().remove("tokens").apply()
    }

    fun connected(): Boolean = tokens()?.accessToken?.isNotBlank() == true
}

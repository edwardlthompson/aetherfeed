package org.aetherfeed.app.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.lanPoolDataStore by preferencesDataStore(name = "lan_pool")

data class LanPoolConfig(
    val hubUrl: String = "",
    val passphrase: String = "",
)

class LanPoolPrefs(private val context: Context) {
    private val hubKey = stringPreferencesKey("hub_url")
    private val passKey = stringPreferencesKey("passphrase")

    suspend fun load(): LanPoolConfig {
        val prefs = context.lanPoolDataStore.data.first()
        return LanPoolConfig(
            hubUrl = prefs[hubKey].orEmpty().trim().trimEnd('/'),
            passphrase = prefs[passKey].orEmpty(),
        )
    }

    suspend fun save(config: LanPoolConfig) {
        context.lanPoolDataStore.edit { prefs ->
            prefs[hubKey] = config.hubUrl.trim().trimEnd('/')
            prefs[passKey] = config.passphrase
        }
    }

    fun observe() =
        context.lanPoolDataStore.data.map { prefs ->
            LanPoolConfig(
                hubUrl = prefs[hubKey].orEmpty().trim().trimEnd('/'),
                passphrase = prefs[passKey].orEmpty(),
            )
        }
}

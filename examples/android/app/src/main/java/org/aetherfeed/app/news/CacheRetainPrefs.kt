package org.aetherfeed.app.news

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.cacheRetainStore by preferencesDataStore(name = "cache_retain")
private val MODE = stringPreferencesKey("mode")

class CacheRetainPrefs(private val context: Context) {
    val state: Flow<CacheRetainMode> = context.cacheRetainStore.data.map { parseCacheRetainMode(it[MODE]) }

    suspend fun current(): CacheRetainMode = state.first()

    suspend fun setMode(mode: CacheRetainMode) {
        context.cacheRetainStore.edit { it[MODE] = encodeCacheRetainMode(mode) }
    }
}

package org.aetherfeed.app.news

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val REFRESH_INTERVAL_HOURS = listOf(1, 3, 6, 12, 24)
const val REFRESH_INTERVAL_DEFAULT = 1

private val Context.feedRefreshStore: DataStore<Preferences> by preferencesDataStore(
    name = "feed_refresh_preferences",
)

private val INTERVAL_HOURS = intPreferencesKey("interval_hours")
private val HISTORY_MODE = stringPreferencesKey("history_mode")
private val HISTORY_COUNT = intPreferencesKey("history_count")
private val HISTORY_DAYS = intPreferencesKey("history_days")
private val WIFI_ONLY = booleanPreferencesKey("wifi_only")

class FeedRefreshPrefs(private val context: Context) {
    val intervalHours: Flow<Int> = context.feedRefreshStore.data.map { prefs ->
        clampInterval(prefs[INTERVAL_HOURS] ?: REFRESH_INTERVAL_DEFAULT)
    }

    val wifiOnly: Flow<Boolean> = context.feedRefreshStore.data.map { prefs ->
        prefs[WIFI_ONLY] ?: true
    }

    val policy: Flow<HistoryPolicy> = context.feedRefreshStore.data.map { prefs ->
        HistoryPolicy(
            mode = runCatching { HistoryMode.valueOf(prefs[HISTORY_MODE] ?: HistoryMode.Count.name) }
                .getOrDefault(HistoryMode.Count),
            count = (prefs[HISTORY_COUNT] ?: HISTORY_COUNT_DEFAULT).coerceAtLeast(1),
            days = (prefs[HISTORY_DAYS] ?: 7).coerceIn(1, HISTORY_DAYS_MAX),
        )
    }

    suspend fun current(): HistoryPolicy = policy.first()

    suspend fun currentIntervalHours(): Int = intervalHours.first()

    suspend fun currentWifiOnly(): Boolean = wifiOnly.first()

    suspend fun setWifiOnly(value: Boolean) {
        context.feedRefreshStore.edit { it[WIFI_ONLY] = value }
    }

    suspend fun setIntervalHours(hours: Int) {
        context.feedRefreshStore.edit { it[INTERVAL_HOURS] = clampInterval(hours) }
    }

    suspend fun setHistoryMode(mode: HistoryMode) {
        context.feedRefreshStore.edit { it[HISTORY_MODE] = mode.name }
    }

    suspend fun setHistoryCount(count: Int) {
        context.feedRefreshStore.edit { it[HISTORY_COUNT] = count.coerceAtLeast(1) }
    }

    suspend fun setHistoryDays(days: Int) {
        context.feedRefreshStore.edit { it[HISTORY_DAYS] = days.coerceIn(1, HISTORY_DAYS_MAX) }
    }

    suspend fun clear() {
        context.feedRefreshStore.edit { it.clear() }
    }
}

fun clampInterval(hours: Int): Int =
    REFRESH_INTERVAL_HOURS.minBy { kotlin.math.abs(it - hours.coerceAtLeast(1)) }

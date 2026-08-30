package org.aetherfeed.app.news

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.newsChromeStore by preferencesDataStore(name = "news_chrome")

private val EXPANDED = stringPreferencesKey("expanded")
private val SIDEBAR_HIDDEN = booleanPreferencesKey("sidebar_hidden")
private val OLDEST_FIRST = booleanPreferencesKey("oldest_first")
private val SOURCE = floatPreferencesKey("source")
private val TIMELINE = floatPreferencesKey("timeline")
private val READER = floatPreferencesKey("reader")
private val FOLDER = stringPreferencesKey("folder")
private val FEED_ID = stringPreferencesKey("feed_id")

data class NewsChrome(
    val expanded: Set<String> = emptySet(),
    val sidebarHidden: Boolean = false,
    val oldestFirst: Boolean = false,
    val source: Float = 0.28f,
    val timeline: Float = 0.34f,
    val reader: Float = 0.38f,
    val folder: String = "",
    val feedId: String = "",
)

fun clampNewsPanes(source: Float, timeline: Float, reader: Float): Triple<Float, Float, Float> {
    val min = 0.14f
    var src = source.coerceAtLeast(min)
    var mid = timeline.coerceAtLeast(min)
    var end = reader.coerceAtLeast(min)
    val sum = src + mid + end
    if (sum <= 0f) return Triple(0.28f, 0.34f, 0.38f)
    src /= sum
    mid /= sum
    end /= sum
    return Triple(src, mid, end)
}

fun toggleNewsExpanded(current: Set<String>, name: String): Set<String> {
    val key = name.trim()
    if (key.isEmpty()) return current
    return if (key in current) current - key else current + key
}

class NewsChromePrefs(private val context: Context) {
    val state: Flow<NewsChrome> = context.newsChromeStore.data.map { prefs ->
        val panes = clampNewsPanes(
            prefs[SOURCE] ?: 0.28f,
            prefs[TIMELINE] ?: 0.34f,
            prefs[READER] ?: 0.38f,
        )
        NewsChrome(
            expanded = prefs[EXPANDED]?.split('\u001f')?.filter { it.isNotBlank() }?.toSet().orEmpty(),
            sidebarHidden = prefs[SIDEBAR_HIDDEN] ?: false,
            oldestFirst = prefs[OLDEST_FIRST] ?: false,
            source = panes.first,
            timeline = panes.second,
            reader = panes.third,
            folder = prefs[FOLDER].orEmpty(),
            feedId = prefs[FEED_ID].orEmpty(),
        )
    }

    suspend fun current(): NewsChrome = state.first()

    suspend fun setExpanded(names: Set<String>) {
        context.newsChromeStore.edit { it[EXPANDED] = names.filter { name -> name.isNotBlank() }.joinToString("\u001f") }
    }

    suspend fun setSidebarHidden(hidden: Boolean) {
        context.newsChromeStore.edit { it[SIDEBAR_HIDDEN] = hidden }
    }

    suspend fun setOldestFirst(oldestFirst: Boolean) {
        context.newsChromeStore.edit { it[OLDEST_FIRST] = oldestFirst }
    }

    suspend fun setLocation(folder: String?, feedId: String?) {
        val name = folder?.trim().orEmpty()
        context.newsChromeStore.edit { prefs ->
            prefs[FOLDER] = name
            prefs[FEED_ID] = feedId?.trim().orEmpty()
        }
    }

    suspend fun setWeights(source: Float, timeline: Float, reader: Float) {
        val panes = clampNewsPanes(source, timeline, reader)
        context.newsChromeStore.edit {
            it[SOURCE] = panes.first
            it[TIMELINE] = panes.second
            it[READER] = panes.third
        }
    }
}

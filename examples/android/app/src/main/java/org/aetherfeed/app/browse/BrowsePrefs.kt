package org.aetherfeed.app.browse

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.aetherfeed.app.news.ReaderMode
import org.aetherfeed.app.ui.navigation.AppDestination

private val Context.browseDataStore by preferencesDataStore(name = "browse_prefs")

data class ModuleBrowseState(
    val unreadOnly: Boolean = false,
    val nodeId: String = "",
    val sort: SortOrder = SortOrder.Newest,
)

class BrowsePrefs(private val context: Context) {
    private fun unreadKey(module: String) = booleanPreferencesKey("${module}_unread")
    private fun nodeKey(module: String) = stringPreferencesKey("${module}_node")
    private fun sortKey(module: String) = stringPreferencesKey("${module}_sort")
    private val destinationKey = stringPreferencesKey("active_destination")
    private val readerModeKey = stringPreferencesKey("reader_mode")

    fun moduleState(module: String): Flow<ModuleBrowseState> =
        context.browseDataStore.data.map { prefs ->
            ModuleBrowseState(
                unreadOnly = prefs[unreadKey(module)] ?: false,
                nodeId = prefs[nodeKey(module)].orEmpty(),
                sort = SortOrder.entries.firstOrNull { it.name == prefs[sortKey(module)] }
                    ?: SortOrder.Newest,
            )
        }

    val destination: Flow<AppDestination> =
        context.browseDataStore.data.map { prefs ->
            AppDestination.entries.firstOrNull { it.name == prefs[destinationKey] }
                ?: AppDestination.News
        }

    val readerMode: Flow<ReaderMode> =
        context.browseDataStore.data.map { prefs ->
            ReaderMode.entries.firstOrNull { it.name == prefs[readerModeKey] } ?: ReaderMode.READING
        }

    suspend fun setUnreadOnly(module: String, value: Boolean) {
        context.browseDataStore.edit { it[unreadKey(module)] = value }
    }

    suspend fun setNodeId(module: String, value: String) {
        context.browseDataStore.edit { it[nodeKey(module)] = value }
    }

    suspend fun setSort(module: String, value: SortOrder) {
        context.browseDataStore.edit { it[sortKey(module)] = value.name }
    }

    suspend fun setDestination(value: AppDestination) {
        context.browseDataStore.edit { it[destinationKey] = value.name }
    }

    suspend fun setReaderMode(value: ReaderMode) {
        context.browseDataStore.edit { it[readerModeKey] = value.name }
    }
}

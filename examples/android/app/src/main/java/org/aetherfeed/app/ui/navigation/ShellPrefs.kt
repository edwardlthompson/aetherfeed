package org.aetherfeed.app.ui.navigation

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.shellStore by preferencesDataStore(name = "app_shell")

private val MODE = stringPreferencesKey("mode")
private val PICK = stringPreferencesKey("library_pick")

class ShellPrefs(private val context: Context) {
    suspend fun current(): AppDestination =
        parseAppDestination(context.shellStore.data.first()[MODE])

    suspend fun currentPick(): LibraryPick =
        parseLibraryPick(context.shellStore.data.first()[PICK])

    suspend fun setMode(destination: AppDestination) {
        context.shellStore.edit { it[MODE] = destination.name }
    }

    suspend fun setPick(pick: LibraryPick) {
        context.shellStore.edit {
            it[PICK] = encodeLibraryPick(pick)
            it[MODE] = pick.destination().name
        }
    }
}

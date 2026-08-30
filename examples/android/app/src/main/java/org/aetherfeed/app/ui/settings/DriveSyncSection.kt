package org.aetherfeed.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.aetherfeed.app.R
import org.aetherfeed.app.data.RoomLibraryRepository
import org.aetherfeed.app.data.SqlCipherVault
import org.aetherfeed.app.sync.DrivePrefs
import org.aetherfeed.app.sync.buildAuthUrl
import org.aetherfeed.app.sync.createPkceVerifier
import org.aetherfeed.app.sync.exchangeAuthCode
import org.aetherfeed.app.sync.syncFeedSources
import org.aetherfeed.app.sync.waitForOauthCode
import org.aetherfeed.app.ui.theme.SpacingMd

@Composable
fun DriveSyncSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { DrivePrefs(context) }
    var clientId by remember { mutableStateOf(prefs.clientId()) }
    var clientSecret by remember { mutableStateOf(prefs.clientSecret()) }
    var passphrase by remember { mutableStateOf(prefs.passphrase()) }
    var status by remember {
        mutableStateOf(if (prefs.connected()) context.getString(R.string.settings_drive_connected) else context.getString(R.string.settings_drive_disconnected))
    }
    val library = remember {
        runCatching { RoomLibraryRepository(SqlCipherVault().open(context).libraryDao()) }.getOrNull()
    }
    LaunchedEffect(library) {
        if (library != null && prefs.connected()) {
            runCatching { syncFeedSources(prefs, library) }
                .onSuccess { status = context.getString(R.string.settings_drive_sync_ok, it.count, it.pulled) }
        }
    }
    fun persist() {
        prefs.setClientId(clientId)
        prefs.setClientSecret(clientSecret)
        prefs.setPassphrase(passphrase)
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(stringResource(R.string.settings_drive_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_drive_hint), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(clientId, { clientId = it }, label = { Text(stringResource(R.string.settings_drive_client_id)) })
        OutlinedTextField(clientSecret, { clientSecret = it }, label = { Text(stringResource(R.string.settings_drive_client_secret)) })
        OutlinedTextField(passphrase, { passphrase = it }, label = { Text(stringResource(R.string.settings_drive_passphrase)) })
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            Button(onClick = {
                persist()
                scope.launch {
                    runCatching {
                        val verifier = createPkceVerifier()
                        val wait = async(Dispatchers.IO) { waitForOauthCode() }
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(buildAuthUrl(prefs.clientId(), verifier))))
                        val code = wait.await()
                        prefs.saveTokens(withContext(Dispatchers.IO) { exchangeAuthCode(prefs.clientId(), prefs.clientSecret(), code, verifier) })
                    }.onSuccess { status = context.getString(R.string.settings_drive_connected) }
                        .onFailure { status = it.message ?: context.getString(R.string.settings_drive_sync_fail) }
                }
            }) { Text(stringResource(R.string.settings_drive_connect)) }
            Button(onClick = {
                persist()
                val lib = library ?: return@Button
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            val result = syncFeedSources(prefs, lib)
                            val cache = org.aetherfeed.app.applock.EncryptedCache(context.filesDir) {
                                org.aetherfeed.app.applock.AppLockHolder.vaultKey()
                            }
                            val stars = lib.stars().map { it.targetId }.toSet()
                            val retain = org.aetherfeed.app.news.CacheRetainPrefs(context).current()
                            org.aetherfeed.app.news.sweepArticleCache(
                                cache, stars, retain, System.currentTimeMillis(), true,
                            )
                            result
                        }
                    }
                        .onSuccess { status = context.getString(R.string.settings_drive_sync_ok, it.count, it.pulled) }
                        .onFailure { status = it.message ?: context.getString(R.string.settings_drive_sync_fail) }
                }
            }) { Text(stringResource(R.string.settings_drive_sync)) }
        }
        Button(onClick = {
            prefs.clearTokens()
            status = context.getString(R.string.settings_drive_disconnected)
        }) { Text(stringResource(R.string.settings_drive_disconnect)) }
        Text(status, modifier = Modifier.testTag("drive-sync-status"))
    }
}

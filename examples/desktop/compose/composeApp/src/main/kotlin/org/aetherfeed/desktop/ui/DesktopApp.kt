package org.aetherfeed.desktop.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.aetherfeed.desktop.notify.DesktopNotificationPoster
import org.aetherfeed.desktop.sync.LanHubHost
import org.aetherfeed.shared.domain.notify.formatNewArticlesNotice
import org.aetherfeed.desktop.ui.navigation.AppDestination
import org.aetherfeed.desktop.ui.theme.ThemeMode
import org.aetherfeed.desktop.unread.totalUnread
import org.aetherfeed.shared.domain.DonationsConfig
import org.aetherfeed.shared.domain.ModuleKind
import org.aetherfeed.shared.domain.ReadStatus
import org.aetherfeed.shared.domain.ReaderSettings

enum class DesktopOverlay {
    None,
    Settings,
    About,
}

@Composable
fun DesktopApp(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onUnreadTotalChange: (Int) -> Unit,
    lanHub: LanHubHost,
    version: String,
    notificationPoster: DesktopNotificationPoster = DesktopNotificationPoster(),
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.News) }
    var overlay by rememberSaveable { mutableStateOf(DesktopOverlay.None) }
    var showAddFeed by rememberSaveable { mutableStateOf(false) }
    var updateCheckEnabled by rememberSaveable { mutableStateOf(false) }
    var readerSettings by rememberSaveable { mutableStateOf(ReaderSettings()) }
    var lanHubUrl by rememberSaveable { mutableStateOf("") }
    var lanPassphrase by rememberSaveable { mutableStateOf("") }
    var lanPoolStatus by rememberSaveable { mutableStateOf("") }
    var stubUnread by rememberSaveable { mutableIntStateOf(0) }

    val unreadDemo = listOf(ReadStatus.Unread, ReadStatus.Read)
    val unreadTotal = totalUnread(unreadDemo) + stubUnread
    LaunchedEffect(unreadTotal) {
        onUnreadTotalChange(unreadTotal)
    }

    val mainVisible = overlay == DesktopOverlay.None
    Scaffold(
        topBar = {
            if (mainVisible) {
                AetherFeedTopBar(onOpenSettings = { overlay = DesktopOverlay.Settings })
            }
        },
        bottomBar = {
            if (mainVisible) {
                AetherFeedBottomBar(destination = destination, onSelect = { destination = it })
            }
        },
        floatingActionButton = {
            if (mainVisible) {
                FloatingActionButton(onClick = { showAddFeed = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add subscription")
                }
            }
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (overlay) {
                DesktopOverlay.None ->
                    DestinationBody(
                        destination = destination,
                        unreadTotal = unreadTotal,
                    )
                DesktopOverlay.Settings ->
                    DesktopSettingsScreen(
                        themeMode = themeMode,
                        updateCheckEnabled = updateCheckEnabled,
                        readerSettings = readerSettings,
                        lanHubUrl = lanHubUrl,
                        lanPassphrase = lanPassphrase,
                        lanPoolStatus = lanPoolStatus,
                        lanHub = lanHub,
                        onThemeModeSelect = onThemeModeChange,
                        onUpdateCheckChange = { updateCheckEnabled = it },
                        onReaderTarget = { readerSettings = readerSettings.copy(target = it) },
                        onReaderScale = { readerSettings = readerSettings.copy(bodyScale = it) },
                        onReaderImages = { readerSettings = readerSettings.copy(imagePolicy = it) },
                        onLanSave = { hub, pass ->
                            lanHubUrl = hub
                            lanPassphrase = pass
                            lanHub.configure(pass, org.aetherfeed.desktop.sync.LanHubDefaults.DEFAULT_PORT)
                            lanPoolStatus = "LAN settings saved (stub host)."
                        },
                        onLanSync = {
                            lanHub.start().fold(
                                onSuccess = { lanPoolStatus = "LAN hub running on port ${lanHub.status().port}." },
                                onFailure = { lanPoolStatus = it.message ?: "LAN sync failed." },
                            )
                        },
                        onAboutOpen = { overlay = DesktopOverlay.About },
                        onBack = { overlay = DesktopOverlay.None },
                    )
                DesktopOverlay.About ->
                    DesktopAboutScreen(
                        version = version,
                        installedFormat = "Compose Desktop (JVM)",
                        updateStatus = if (updateCheckEnabled) "Update check enabled (stub)." else "Update check off.",
                        donations = DonationsConfig(),
                        onBack = { overlay = DesktopOverlay.Settings },
                    )
            }
        }
    }

    if (showAddFeed) {
        AddFeedDialog(
            onDismiss = { showAddFeed = false },
            onConfirm = { _, _, kind ->
                showAddFeed = false
                if (kind == ModuleKind.News) {
                    stubUnread += 1
                    formatNewArticlesNotice(1)?.let { notificationPoster.post(it) }
                }
            },
        )
    }
}

@Composable
private fun DestinationBody(
    destination: AppDestination,
    unreadTotal: Int,
) {
    val message =
        when (destination) {
            AppDestination.News -> "News — $unreadTotal unread (stub list)."
            AppDestination.Podcasts -> "Podcasts — empty body (stub)."
            AppDestination.Booru -> "Booru — demo grid (stub)."
        }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}
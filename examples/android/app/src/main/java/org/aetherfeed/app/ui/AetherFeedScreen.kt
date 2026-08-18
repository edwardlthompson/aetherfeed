package org.aetherfeed.app.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.aetherfeed.app.about.DonationsConfig
import org.aetherfeed.app.ui.components.AetherFeedScaffold
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.theme.ThemeMode

@Composable
fun AetherFeedScreen(
    snackbarHostState: SnackbarHostState,
    themeMode: ThemeMode,
    isOnline: Boolean,
    showAbout: Boolean,
    showSettings: Boolean,
    updateCheckEnabled: Boolean,
    appVersion: String,
    installedFormat: String,
    updateStatus: String,
    donations: DonationsConfig,
    canApplyUpdate: Boolean,
    onThemeToggle: () -> Unit,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onAboutOpen: () -> Unit,
    onAboutClose: () -> Unit,
    onSettingsOpen: () -> Unit,
    onSettingsClose: () -> Unit,
    onUpdateCheckChange: (Boolean) -> Unit,
    onApplyUpdate: () -> Unit,
    unreadTotal: Int = 0,
) {
    var destination by remember { mutableStateOf(AppDestination.News) }
    AetherFeedScaffold(
        snackbarHostState = snackbarHostState,
        bottomBar = {
            AetherFeedBottomBar(
                destination = destination,
                onSelect = { item ->
                    destination = item
                    if (item == AppDestination.Settings) {
                        onSettingsOpen()
                    }
                },
            )
        },
        topBar = {
            AetherFeedTopBar(
                themeMode = themeMode,
                onThemeToggle = onThemeToggle,
                onSettingsOpen = onSettingsOpen,
                onAboutOpen = onAboutOpen,
            )
        },
    ) { innerPadding ->
        AetherFeedBody(
            destination = destination,
            innerPadding = innerPadding,
            showAbout = showAbout,
            showSettings = showSettings,
            themeMode = themeMode,
            updateCheckEnabled = updateCheckEnabled,
            appVersion = appVersion,
            installedFormat = installedFormat,
            updateStatus = updateStatus,
            donations = donations,
            canApplyUpdate = canApplyUpdate,
            unreadTotal = unreadTotal,
            onThemeModeSelect = onThemeModeSelect,
            onUpdateCheckChange = onUpdateCheckChange,
            onApplyUpdate = onApplyUpdate,
            onAboutClose = onAboutClose,
            onSettingsClose = onSettingsClose,
        )
    }
}

package org.aetherfeed.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.about.DonationsConfig
import org.aetherfeed.app.ui.about.AboutScreen
import org.aetherfeed.app.ui.components.AetherFeedScaffold
import org.aetherfeed.app.ui.components.ThemeToggle
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.navigation.ModulePane
import org.aetherfeed.app.ui.settings.SettingsScreen
import org.aetherfeed.app.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
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
            NavigationBar {
                AppDestination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = {
                            destination = item
                            if (item == AppDestination.Settings) {
                                onSettingsOpen()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.contentDescriptionRes),
                            )
                        },
                        label = { Text(stringResource(item.labelRes)) },
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title)) },
                actions = {
                    IconButton(onClick = onSettingsOpen) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_open),
                        )
                    }
                    IconButton(onClick = onAboutOpen) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.about_open),
                        )
                    }
                    ThemeToggle(themeMode = themeMode, onToggle = onThemeToggle)
                },
            )
        },
    ) { innerPadding ->
        when {
            showSettings -> SettingsScreen(
                themeMode = themeMode,
                updateCheckEnabled = updateCheckEnabled,
                onThemeModeSelect = onThemeModeSelect,
                onUpdateCheckChange = onUpdateCheckChange,
                onBack = onSettingsClose,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            showAbout -> AboutScreen(
                version = appVersion,
                installedFormat = installedFormat,
                updateStatus = updateStatus,
                donations = donations,
                canApplyUpdate = canApplyUpdate,
                onApplyUpdate = onApplyUpdate,
                onBack = onAboutClose,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            else -> when (destination) {
                AppDestination.Settings -> SettingsScreen(
                    themeMode = themeMode,
                    updateCheckEnabled = updateCheckEnabled,
                    onThemeModeSelect = onThemeModeSelect,
                    onUpdateCheckChange = onUpdateCheckChange,
                    onBack = onSettingsClose,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
                AppDestination.News -> ModulePane(
                    title = stringResource(R.string.nav_news),
                    body = stringResource(R.string.pane_news_body),
                    unread = unreadTotal,
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.Podcasts -> ModulePane(
                    title = stringResource(R.string.nav_podcasts),
                    body = stringResource(R.string.pane_podcasts_body),
                    unread = unreadTotal,
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.Booru -> ModulePane(
                    title = stringResource(R.string.nav_booru),
                    body = stringResource(R.string.pane_booru_body),
                    unread = unreadTotal,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

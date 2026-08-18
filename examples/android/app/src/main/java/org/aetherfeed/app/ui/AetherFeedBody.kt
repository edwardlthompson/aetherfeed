package org.aetherfeed.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.about.DonationsConfig
import org.aetherfeed.app.ui.about.AboutScreen
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.navigation.ModulePane
import org.aetherfeed.app.ui.settings.SettingsScreen
import org.aetherfeed.app.ui.theme.ThemeMode

@Composable
fun AetherFeedBody(
    destination: AppDestination,
    innerPadding: PaddingValues,
    showAbout: Boolean,
    showSettings: Boolean,
    themeMode: ThemeMode,
    updateCheckEnabled: Boolean,
    appVersion: String,
    installedFormat: String,
    updateStatus: String,
    donations: DonationsConfig,
    canApplyUpdate: Boolean,
    unreadTotal: Int,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onUpdateCheckChange: (Boolean) -> Unit,
    onApplyUpdate: () -> Unit,
    onAboutClose: () -> Unit,
    onSettingsClose: () -> Unit,
) {
    when {
        showSettings || destination == AppDestination.Settings -> SettingsScreen(
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
        destination == AppDestination.News -> ModulePane(
            title = stringResource(R.string.nav_news),
            body = stringResource(R.string.pane_news_body),
            unread = unreadTotal,
            modifier = Modifier.padding(innerPadding),
        )
        destination == AppDestination.Podcasts -> ModulePane(
            title = stringResource(R.string.nav_podcasts),
            body = stringResource(R.string.pane_podcasts_body),
            unread = unreadTotal,
            modifier = Modifier.padding(innerPadding),
        )
        else -> ModulePane(
            title = stringResource(R.string.nav_booru),
            body = stringResource(R.string.pane_booru_body),
            unread = unreadTotal,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

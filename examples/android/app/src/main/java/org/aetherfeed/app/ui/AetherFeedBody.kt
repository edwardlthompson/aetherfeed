package org.aetherfeed.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.aetherfeed.app.about.DonationsConfig
import org.aetherfeed.app.ui.about.AboutScreen
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.navigation.BoardsDestination
import org.aetherfeed.app.ui.news.NewsPane
import org.aetherfeed.app.ui.podcasts.PodcastsPane
import org.aetherfeed.app.ui.settings.SettingsScreen
import org.aetherfeed.app.ui.theme.ThemeMode

@Composable
fun AetherFeedBody(
    destination: AppDestination,
    pick: org.aetherfeed.app.ui.navigation.LibraryPick = org.aetherfeed.app.ui.navigation.LibraryPick.All(AppDestination.News),
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
    unread: UnreadCounts,
    onUnread: (UnreadCounts) -> Unit,
    onNewsRefreshReady: ((() -> Unit)?) -> Unit,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onUpdateCheckChange: (Boolean) -> Unit,
    onApplyUpdate: () -> Unit,
    onAboutClose: () -> Unit,
    onSettingsClose: () -> Unit,
    newsFolder: String? = null,
    newsFeedId: String? = null,
    newsArticleId: String? = null,
    onNewsNav: (String?, String?, String?) -> Unit = { _, _, _ -> },
    onLibraryPick: (org.aetherfeed.app.ui.navigation.LibraryPick) -> Unit = {},
    onFocusedMode: (AppDestination) -> Unit = {},
    actions: ModeActionBus? = null,
) {
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
        destination == AppDestination.News -> NewsPane(
            modifier = Modifier.padding(innerPadding),
            savedFolder = newsFolder,
            savedFeedId = newsFeedId,
            savedArticleId = newsArticleId,
            onNavChange = onNewsNav,
            onRefreshReady = onNewsRefreshReady,
            onUnread = { news -> onUnread(unread.copy(news = news)) },
            onUnreadCounts = onUnread,
            onLibraryPick = onLibraryPick,
            onFocusedMode = onFocusedMode,
            actions = actions,
        )
        destination == AppDestination.Podcasts -> PodcastsPane(
            modifier = Modifier.padding(innerPadding),
            actions = actions,
            pick = pick,
            unread = unread,
            onLibraryPick = onLibraryPick,
        )
        destination == AppDestination.Booru -> BoardsDestination(
            modifier = Modifier.padding(innerPadding),
            actions = actions,
            pick = pick,
            unread = unread,
            onLibraryPick = onLibraryPick,
        )
    }
}

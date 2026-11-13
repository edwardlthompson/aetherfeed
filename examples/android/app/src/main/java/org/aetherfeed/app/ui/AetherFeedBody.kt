package org.aetherfeed.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.about.DonationsConfig
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.ui.about.AboutScreen
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.navigation.ModulePane
import org.aetherfeed.app.domain.BooruPost
import org.aetherfeed.app.ui.booru.BooruScreen
import org.aetherfeed.app.news.ArticleTarget
import org.aetherfeed.app.news.ImagePolicy
import org.aetherfeed.app.news.ReaderSettings
import org.aetherfeed.app.ui.news.NewsScreen
import org.aetherfeed.app.ui.podcasts.PodcastsScreen
import org.aetherfeed.app.ui.settings.SettingsScreen
import org.aetherfeed.app.ui.theme.ThemeMode

@Composable
fun AetherFeedBody(
    destination: AppDestination,
    innerPadding: PaddingValues,
    newsReading: Boolean = false,
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
    newsArticles: List<Article> = emptyList(),
    newsFeeds: List<Feed> = emptyList(),
    newsStarredIds: Set<String> = emptySet(),
    newsUnreadIds: Set<String> = emptySet(),
    onNewsArticleOpen: (Article) -> Unit = {},
    onNewsToggleStar: (Article) -> Unit = {},
    onNewsParkUnread: (Article) -> Unit = {},
    onNewsReadingChange: (Boolean) -> Unit = {},
    podcastShows: List<Feed> = emptyList(),
    booruPosts: List<BooruPost> = emptyList(),
    booruStarredIds: Set<String> = emptySet(),
    onBooruToggleStar: (BooruPost) -> Unit = {},
    onThemeModeSelect: (ThemeMode) -> Unit,
    onUpdateCheckChange: (Boolean) -> Unit,
    onApplyUpdate: () -> Unit,
    onAboutOpen: () -> Unit,
    onAboutClose: () -> Unit,
    onSettingsClose: () -> Unit,
    hermesPoolStatus: String = "",
    lanPoolStatus: String = "",
    lanHubUrl: String = "",
    lanPassphrase: String = "",
    onLanSave: (String, String) -> Unit = { _, _ -> },
    onLanSync: () -> Unit = {},
    isOnline: Boolean = true,
    readerSettings: ReaderSettings = ReaderSettings(),
    onReaderTarget: (ArticleTarget) -> Unit = {},
    onReaderScale: (Float) -> Unit = {},
    onReaderImages: (ImagePolicy) -> Unit = {},
) {
    when {
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
        showSettings -> SettingsScreen(
            themeMode = themeMode,
            updateCheckEnabled = updateCheckEnabled,
            hermesPoolStatus = hermesPoolStatus,
            lanPoolStatus = lanPoolStatus,
            lanHubUrl = lanHubUrl,
            lanPassphrase = lanPassphrase,
            onThemeModeSelect = onThemeModeSelect,
            onUpdateCheckChange = onUpdateCheckChange,
            onLanSave = onLanSave,
            onLanSync = onLanSync,
            readerSettings = readerSettings,
            onReaderTarget = onReaderTarget,
            onReaderScale = onReaderScale,
            onReaderImages = onReaderImages,
            onAboutOpen = onAboutOpen,
            onBack = onSettingsClose,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
        destination == AppDestination.News -> NewsScreen(
            articles = newsArticles,
            feeds = newsFeeds,
            starredIds = newsStarredIds,
            unreadIds = newsUnreadIds,
            onArticleOpen = onNewsArticleOpen,
            onToggleStar = onNewsToggleStar,
            onParkUnread = onNewsParkUnread,
            onReadingChange = onNewsReadingChange,
            readerSettings = readerSettings,
            isOnline = isOnline,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    if (newsReading) {
                        PaddingValues(bottom = innerPadding.calculateBottomPadding())
                    } else {
                        innerPadding
                    },
                ),
        )
        destination == AppDestination.Podcasts -> PodcastsScreen(
            shows = podcastShows,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
        destination == AppDestination.Booru -> BooruScreen(
            posts = booruPosts,
            starredIds = booruStarredIds,
            onToggleStar = onBooruToggleStar,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
        else -> ModulePane(
            title = stringResource(R.string.nav_booru),
            body = stringResource(R.string.pane_booru_body),
            unread = unreadTotal,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

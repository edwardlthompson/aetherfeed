package org.aetherfeed.app.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.aetherfeed.app.about.DonationsConfig
import org.aetherfeed.app.ui.components.AetherFeedScaffold
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.navigation.destination
import org.aetherfeed.app.ui.navigation.ShellPrefs
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shellPrefs = remember { ShellPrefs(context) }
    var pick by remember { mutableStateOf<org.aetherfeed.app.ui.navigation.LibraryPick>(org.aetherfeed.app.ui.navigation.LibraryPick.All(AppDestination.News)) }
    var destination by remember { mutableStateOf(AppDestination.News) }
    var newsFolder by rememberSaveable { mutableStateOf<String?>(null) }
    var newsFeedId by rememberSaveable { mutableStateOf<String?>(null) }
    var newsArticleId by rememberSaveable { mutableStateOf<String?>(null) }
    var unread by remember { mutableStateOf(UnreadCounts(news = unreadTotal)) }
    var newsRefresh by remember { mutableStateOf<(() -> Unit)?>(null) }
    val actions = remember { ModeActionBus() }
    LaunchedEffect(Unit) {
        pick = runCatching { shellPrefs.currentPick() }.getOrDefault(org.aetherfeed.app.ui.navigation.LibraryPick.All(AppDestination.News))
        destination = pick.destination()
    }
    AetherFeedScaffold(
        snackbarHostState = snackbarHostState,
        bottomBar = {
            if (!showAbout && !showSettings) {
                ModeActionBar(destination = destination, bus = actions)
            }
        },
        topBar = {
            AetherFeedTopBar(
                themeMode = themeMode,
                onThemeToggle = onThemeToggle,
                onSettingsOpen = onSettingsOpen,
                onAboutOpen = onAboutOpen,
                onRefresh = if (!showAbout && !showSettings) {
                    { newsRefresh?.invoke() }
                } else {
                    null
                },
            )
        },
    ) { innerPadding ->
        AetherFeedBody(
            destination = destination,
            pick = pick,
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
            unread = unread,
            onUnread = { unread = it },
            onNewsRefreshReady = { newsRefresh = it },
            onThemeModeSelect = onThemeModeSelect,
            onUpdateCheckChange = onUpdateCheckChange,
            onApplyUpdate = onApplyUpdate,
            onAboutClose = onAboutClose,
            onSettingsClose = onSettingsClose,
            newsFolder = newsFolder,
            newsFeedId = newsFeedId,
            newsArticleId = newsArticleId,
            onNewsNav = { folder, feedId, articleId ->
                newsFolder = folder
                newsFeedId = feedId
                newsArticleId = articleId
            },
            onLibraryPick = { next ->
                pick = next
                destination = next.destination()
                scope.launch { shellPrefs.setPick(next) }
            },
            onFocusedMode = { destination = it },
            actions = actions,
        )
    }
}

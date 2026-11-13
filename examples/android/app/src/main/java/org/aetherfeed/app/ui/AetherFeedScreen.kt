package org.aetherfeed.app.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.aetherfeed.app.about.DonationsConfig
import org.aetherfeed.app.booru.DanbooruAdapter
import org.aetherfeed.app.browse.BrowsePrefs
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus
import org.aetherfeed.app.domain.Star
import org.aetherfeed.app.sync.LanPoolSync
import org.aetherfeed.app.sync.ReadReceiptSync
import org.aetherfeed.app.sync.SubscriptionPoolSync
import org.aetherfeed.app.ui.components.AetherFeedScaffold
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.news.ArticleTarget
import org.aetherfeed.app.news.ImagePolicy
import org.aetherfeed.app.news.ReaderSettings
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
    hermesPoolStatus: String = "",
    lanPoolStatus: String = "",
    lanHubUrl: String = "",
    lanPassphrase: String = "",
    onLanSave: (String, String) -> Unit = { _, _ -> },
    onLanSync: () -> Unit = {},
    readerSettings: ReaderSettings = ReaderSettings(),
    onReaderTarget: (ArticleTarget) -> Unit = {},
    onReaderScale: (Float) -> Unit = {},
    onReaderImages: (ImagePolicy) -> Unit = {},
    libraryRepository: LibraryRepository? = null,
    readReceiptSync: ReadReceiptSync? = null,
    subscriptionPoolSync: SubscriptionPoolSync? = null,
    lanPoolSync: LanPoolSync? = null,
    scope: CoroutineScope? = null,
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
    val uiScope = rememberCoroutineScope()
    val browsePrefs = remember { BrowsePrefs(context) }
    val destination by browsePrefs.destination.collectAsState(AppDestination.News)
    var showAddFeed by remember { mutableStateOf(false) }
    var newsArticles by remember { mutableStateOf<List<Article>>(emptyList()) }
    var newsStarredIds by remember { mutableStateOf(setOf<String>()) }
    var newsUnreadIds by remember { mutableStateOf(setOf<String>()) }
    var newsReading by remember { mutableStateOf(false) }
    var podcastShows by remember { mutableStateOf<List<Feed>>(emptyList()) }
    var newsFeeds by remember { mutableStateOf<List<Feed>>(emptyList()) }
    val demoBooruPosts =
        remember {
            DanbooruAdapter().mapPosts(
                """
                [
                  {"id": 1001, "tag_string": "landscape scenery safe",
                   "file_url": "https://example.test/booru/1001.jpg",
                   "preview_file_url": "https://example.test/booru/1001-preview.jpg"},
                  {"id": 1002, "tag_string": "abstract art safe",
                   "file_url": "https://example.test/booru/1002.jpg",
                   "preview_file_url": "https://example.test/booru/1002-preview.jpg"}
                ]
                """.trimIndent(),
                sourceId = "demo-danbooru",
            )
        }
    var booruStarredIds by remember { mutableStateOf(setOf<String>()) }

    suspend fun reloadNews() {
        val library = libraryRepository ?: return
        val articles = library.articles(null).sortedByDescending { it.publishedAt ?: 0L }.take(80)
        newsArticles = articles
        newsUnreadIds =
            articles
                .filter { library.readState(it.id)?.status != ReadStatus.Read }
                .map { it.id }
                .toSet()
        newsStarredIds = library.stars().filter { it.module == ModuleKind.News }.map { it.targetId }.toSet()
        podcastShows = library.feeds().filter { it.kind == ModuleKind.Podcast }
        newsFeeds = library.feeds().filter { it.kind == ModuleKind.News }
    }

    LaunchedEffect(libraryRepository, destination, showSettings) {
        reloadNews()
    }

    AetherFeedScaffold(
        snackbarHostState = snackbarHostState,
        bottomBar = {
            AetherFeedBottomBar(
                destination = destination,
                onSelect = { item ->
                    uiScope.launch { browsePrefs.setDestination(item) }
                    onSettingsClose()
                    onAboutClose()
                },
            )
        },
        topBar = {
            if (!newsReading) {
                AetherFeedTopBar(onSettingsOpen = onSettingsOpen)
            }
        },
        floatingActionButton = {
            if (!showAbout && !showSettings && !newsReading) {
                AddFeedFab(onClick = { showAddFeed = true })
            }
        },
    ) { innerPadding ->
        if (showAddFeed) {
            AddFeedDialog(
                onDismiss = { showAddFeed = false },
                onConfirm = { url, title, kind ->
                    showAddFeed = false
                    val library = libraryRepository
                    val jobScope = scope
                    if (library != null && jobScope != null) {
                        jobScope.launch {
                            val now = System.currentTimeMillis()
                            library.upsertFeed(
                                Feed(
                                    id = url,
                                    title = title.ifBlank { url },
                                    url = url,
                                    kind = kind,
                                    updatedAt = now,
                                ),
                            )
                            subscriptionPoolSync?.publishFromLocal()
                            lanPoolSync?.syncNow()
                            reloadNews()
                        }
                    }
                },
            )
        }
        AetherFeedBody(
            destination = destination,
            innerPadding = innerPadding,
            newsReading = newsReading,
            showAbout = showAbout,
            showSettings = showSettings,
            themeMode = themeMode,
            updateCheckEnabled = updateCheckEnabled,
            appVersion = appVersion,
            installedFormat = installedFormat,
            updateStatus = updateStatus,
            donations = donations,
            canApplyUpdate = canApplyUpdate,
            unreadTotal = unreadTotal.coerceAtLeast(newsUnreadIds.size),
            newsArticles = newsArticles,
            newsFeeds = newsFeeds,
            newsStarredIds = newsStarredIds,
            newsUnreadIds = newsUnreadIds,
            onNewsArticleOpen = { article ->
                newsUnreadIds = newsUnreadIds - article.id
                val library = libraryRepository
                val sync = readReceiptSync
                val jobScope = scope
                if (library != null && jobScope != null) {
                    jobScope.launch {
                        library.upsertReadState(
                            ReadState(
                                targetId = article.id,
                                module = ModuleKind.News,
                                status = ReadStatus.Read,
                                updatedAt = System.currentTimeMillis(),
                            ),
                        )
                        sync?.publishFromLocal()
                        lanPoolSync?.syncNow()
                        reloadNews()
                    }
                }
            },
            onNewsReadingChange = { newsReading = it },
            podcastShows = podcastShows,
            onNewsParkUnread = { article ->
                newsUnreadIds = newsUnreadIds + article.id
                val library = libraryRepository
                val sync = readReceiptSync
                val jobScope = scope
                if (library != null && jobScope != null) {
                    jobScope.launch {
                        library.upsertReadState(
                            ReadState(
                                targetId = article.id,
                                module = ModuleKind.News,
                                status = ReadStatus.Unread,
                                updatedAt = System.currentTimeMillis(),
                            ),
                        )
                        sync?.publishFromLocal()
                        lanPoolSync?.syncNow()
                        reloadNews()
                    }
                }
            },
            onNewsToggleStar = { article ->
                val starring = article.id !in newsStarredIds
                newsStarredIds =
                    if (starring) {
                        newsStarredIds + article.id
                    } else {
                        newsStarredIds - article.id
                    }
                val library = libraryRepository
                val jobScope = scope
                if (library != null && jobScope != null) {
                    jobScope.launch {
                        if (starring) {
                            library.upsertStar(
                                Star(
                                    targetId = article.id,
                                    module = ModuleKind.News,
                                    createdAt = System.currentTimeMillis(),
                                ),
                            )
                        } else {
                            library.removeStar(article.id)
                        }
                        lanPoolSync?.syncNow()
                    }
                }
            },
            booruPosts = demoBooruPosts,
            booruStarredIds = booruStarredIds,
            onBooruToggleStar = { post ->
                booruStarredIds =
                    if (post.id in booruStarredIds) {
                        booruStarredIds - post.id
                    } else {
                        booruStarredIds + post.id
                    }
            },
            onThemeModeSelect = onThemeModeSelect,
            onUpdateCheckChange = onUpdateCheckChange,
            onApplyUpdate = onApplyUpdate,
            onAboutOpen = onAboutOpen,
            onAboutClose = onAboutClose,
            onSettingsClose = onSettingsClose,
            hermesPoolStatus = hermesPoolStatus,
            lanPoolStatus = lanPoolStatus,
            lanHubUrl = lanHubUrl,
            lanPassphrase = lanPassphrase,
            onLanSave = onLanSave,
            onLanSync = onLanSync,
            isOnline = isOnline,
            readerSettings = readerSettings,
            onReaderTarget = onReaderTarget,
            onReaderScale = onReaderScale,
            onReaderImages = onReaderImages,
        )
    }
}

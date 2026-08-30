package org.aetherfeed.app.ui.news

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.aetherfeed.app.R
import org.aetherfeed.app.applock.AppLockHolder
import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.data.RoomLibraryRepository
import org.aetherfeed.app.data.SqlCipherVault
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus
import org.aetherfeed.app.domain.Star
import org.aetherfeed.app.downloads.isUnmeteredNetwork
import org.aetherfeed.app.news.CacheProgress
import org.aetherfeed.app.news.EncryptedArticleIndex
import org.aetherfeed.app.news.FeedRefreshPrefs
import org.aetherfeed.app.news.FeedRefreshScheduler
import org.aetherfeed.app.news.HttpFeedFetcher
import org.aetherfeed.app.news.NewsChrome
import org.aetherfeed.app.news.NewsChromePrefs
import org.aetherfeed.app.news.NewsRepository
import org.aetherfeed.app.news.VaultNewsRepository
import org.aetherfeed.app.news.canFetchNews
import org.aetherfeed.app.news.folderLabel
import org.aetherfeed.app.news.groupFeedsByFolder
import org.aetherfeed.app.news.hydrateNeighbors
import org.aetherfeed.app.news.CacheRetainPrefs
import org.aetherfeed.app.news.headlinesForPick
import org.aetherfeed.app.news.sweepArticleCache
import org.aetherfeed.app.ui.UnreadCounts
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.navigation.LibraryPick
import org.aetherfeed.app.ui.navigation.destination
import org.aetherfeed.app.ui.navigation.expandKeys
import org.aetherfeed.app.news.refreshAllNews
import org.aetherfeed.app.news.sweepNotice
import org.aetherfeed.app.news.sortArticles
import org.aetherfeed.app.news.NewsUnreadMaps
import org.aetherfeed.app.news.restoreNewsFeed
import org.aetherfeed.app.readerimport.applySeedLibrary
import org.aetherfeed.app.readerimport.isSmokeDemo
import org.aetherfeed.app.readerimport.pruneSmokeLibrary
import org.aetherfeed.app.ui.ModeActionBus

@Composable
fun NewsPane(
    modifier: Modifier = Modifier,
    library: LibraryRepository? = null,
    repository: NewsRepository? = null,
    savedFolder: String? = null,
    savedFeedId: String? = null,
    savedArticleId: String? = null,
    onNavChange: (String?, String?, String?) -> Unit = { _, _, _ -> },
    onRefreshReady: ((() -> Unit)?) -> Unit = {},
    onUnread: (Int) -> Unit = {},
    onUnreadCounts: (UnreadCounts) -> Unit = {},
    onLibraryPick: (LibraryPick) -> Unit = {},
    onFocusedMode: (AppDestination) -> Unit = {},
    actions: ModeActionBus? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vault = remember(library) {
        library ?: runCatching { RoomLibraryRepository(SqlCipherVault().open(context).libraryDao()) }.getOrNull()
    }
    val refreshPrefs = remember { FeedRefreshPrefs(context) }
    val chromePrefs = remember { NewsChromePrefs(context) }
    val chrome by chromePrefs.state.collectAsState(NewsChrome())
    val cache = remember { EncryptedCache(context.filesDir) { AppLockHolder.vaultKey() } }
    val articleIndex = remember { EncryptedArticleIndex(cache) }
    val news = remember(vault, repository) {
        repository ?: vault?.let {
            VaultNewsRepository(it, HttpFeedFetcher(), policy = { refreshPrefs.current() }, index = articleIndex)
        }
    }
    var feeds by remember { mutableStateOf(listOf<Feed>()) }
    var selectedFolder by remember { mutableStateOf(savedFolder) }
    var selectedFeed by remember { mutableStateOf<Feed?>(null) }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }
    var articles by remember { mutableStateOf(listOf<Article>()) }
    var starred by remember { mutableStateOf(setOf<String>()) }
    var readIds by remember { mutableStateOf(setOf<String>()) }
    var statusText by remember { mutableStateOf("") }
    var refreshing by remember { mutableStateOf(false) }
    var cacheUi by remember { mutableStateOf(NewsCacheUi()) }
    var unreadMaps by remember { mutableStateOf(NewsUnreadMaps()) }
    var counts by remember { mutableStateOf(UnreadCounts()) }
    var prefetchJob by remember { mutableStateOf<Job?>(null) }
    var openingId by remember { mutableStateOf<String?>(null) }
    var extraFeeds by remember { mutableStateOf(listOf<Feed>()) }
    var pickGen by remember { mutableStateOf(0) }
    var pick by remember {
        mutableStateOf(
            savedFeedId?.let { LibraryPick.Source(AppDestination.News, savedFolder.orEmpty(), it) }
                ?: LibraryPick.All(AppDestination.News),
        )
    }
    val retainPrefs = remember { CacheRetainPrefs(context) }
    val folderOf: (Feed) -> String = { feed ->
        (news as? VaultNewsRepository)?.folderFor(feed) ?: feed.folderLabel()
    }
    val sorted = remember(articles, chrome.oldestFirst) { sortArticles(articles, chrome.oldestFirst) }
    fun emitNav(folder: String?, feedId: String?, articleId: String?) {
        onNavChange(folder, feedId, articleId)
        if (articleId == null) scope.launch { chromePrefs.setLocation(folder, feedId) }
    }
    suspend fun reloadFeeds() {
        extraFeeds = vault?.feeds().orEmpty()
        feeds = extraFeeds.filter { it.kind == ModuleKind.News }
        starred = vault?.stars()?.map { it.targetId }?.toSet().orEmpty()
        readIds = vault?.readStates()?.filter { it.status == ReadStatus.Read }?.map { it.targetId }?.toSet().orEmpty()
        if (selectedFolder == null && feeds.isNotEmpty()) selectedFolder = folderOf(feeds.first())
    }
    fun refreshFeed(feed: Feed) {
        launchRefreshFeed(scope, context, refreshPrefs, news, vault, feed, { refreshing = it }, { statusText = it }, { articles = it }, { readIds = readIds + it })
    }
    fun applyPick(next: LibraryPick) {
        pick = next
        pickGen += 1
        val gen = pickGen
        selectedArticle = null
        selectedFolder = folderFromPick(next, selectedFolder)
        selectedFeed = feeds.firstOrNull { it.id == (next as? LibraryPick.Source)?.sourceId }
        onLibraryPick(next)
        onFocusedMode(next.destination())
        emitNav(selectedFolder, selectedFeed?.id, null)
        if (next.destination() != AppDestination.News && next !is LibraryPick.Unified) return
        scope.launch {
            chromePrefs.setExpanded(chrome.expanded + next.expandKeys())
            val allowNet = canFetchNews(refreshPrefs.currentWifiOnly(), isUnmeteredNetwork(context))
            val rows = headlinesForPick(next, news, extraFeeds, folderOf, allowNet)
            if (acceptPickGen(gen, pickGen)) {
                articles = articlesForPick(next, rows, org.aetherfeed.app.news.unifiedExtras(extraFeeds))
            }
        }
    }
    fun openFeed(feed: Feed) {
        applyPick(LibraryPick.Source(AppDestination.News, folderOf(feed), feed.id))
        refreshFeed(feed)
    }
    fun refreshAll() {
        val repo = news ?: return
        refreshing = true
        scope.launch {
            if (!canFetchNews(refreshPrefs.currentWifiOnly(), isUnmeteredNetwork(context))) {
                statusText = context.getString(R.string.news_wifi_blocked)
                refreshing = false
                return@launch
            }
            statusText = sweepNotice(context, refreshAllNews(repo, feeds))
            val allowNet = canFetchNews(refreshPrefs.currentWifiOnly(), isUnmeteredNetwork(context))
            articles = articlesForPick(
                pick,
                headlinesForPick(pick, news, extraFeeds, folderOf, allowNet),
                org.aetherfeed.app.news.unifiedExtras(extraFeeds),
            )
            withContext(Dispatchers.IO) {
                sweepArticleCache(cache, starred, retainPrefs.current(), System.currentTimeMillis(), true)
            }
            refreshing = false
        }
    }
    val refreshNow by rememberUpdatedState<() -> Unit>({ refreshAll() })
    DisposableEffect(Unit) {
        onRefreshReady { refreshNow() }
        onDispose { onRefreshReady(null) }
    }
    LaunchedEffect(vault) {
        if (vault != null) {
            runCatching { applySeedLibrary(context, vault) }
            runCatching { pruneSmokeLibrary(vault) }
            runCatching { FeedRefreshScheduler.ensureDefault(context) }
        }
        runCatching { reloadFeeds() }
        val remembered = runCatching { chromePrefs.current() }.getOrNull()
        val restored = restoreNewsFeed(
            feeds,
            savedFolder ?: remembered?.folder?.ifBlank { null },
            savedFeedId ?: remembered?.feedId?.ifBlank { null },
            folderOf,
            ::isSmokeDemo,
        )
        if (restored != null && selectedFeed == null) {
            applyPick(LibraryPick.Source(AppDestination.News, folderOf(restored), restored.id))
        } else {
            applyPick(pick)
        }
        withContext(Dispatchers.IO) {
            sweepArticleCache(cache, starred, retainPrefs.current(), System.currentTimeMillis(), false)
        }
    }
    fun startPrefetch(fromId: String?) {
        prefetchJob?.cancel()
        prefetchJob = scope.startUnreadPrefetch(
            context, refreshPrefs, news, feeds, chrome.oldestFirst, fromId, readIds, cache,
        ) { cacheUi = it(cacheUi) }
    }
    fun openArticle(article: Article) {
        focusedModeFor(article)?.let { mode ->
            onFocusedMode(mode)
            selectedArticle = article
            emitNav(selectedFolder, selectedFeed?.id, article.id)
            return
        }
        val known = sessionHtml(article.id)
        openingId = article.id
        immediateReaderHtml(article.id, cache)?.let { selectedArticle = article.copy(contentHtml = it) }
        readIds = readIds + article.id
        emitNav(selectedFolder, selectedFeed?.id, article.id)
        prefetchJob?.cancel()
        scope.launch {
            val allowNet = canFetchNews(refreshPrefs.currentWifiOnly(), isUnmeteredNetwork(context))
            val neighborJob = async(Dispatchers.IO) { hydrateNeighbors(cache, sorted, article.id, allowNet) }
            val paint = openArticleBody(article, cache, sorted, allowNet, known) { done, total ->
                scope.launch { cacheUi = cacheUi.copy(article = CacheProgress(done, total)) }
            }
            if (paint.html != null && openingId == article.id) {
                cacheUi = cacheUi.copy(cached = cacheUi.cached + paint.cached, htmlRev = cacheUi.htmlRev + 1)
                selectedArticle = article.copy(contentHtml = paint.html)
                paint.thumb?.let { cacheUi = cacheUi.copy(thumbs = cacheUi.thumbs + (article.id to it)) }
            }
            vault?.upsertReadState(ReadState(article.id, ModuleKind.News, ReadStatus.Read, System.currentTimeMillis()))
            val extra = neighborJob.await()
            if (extra.isNotEmpty() && openingId == article.id) {
                cacheUi = cacheUi.copy(cached = cacheUi.cached + extra.keys, htmlRev = cacheUi.htmlRev + 1)
            }
        }
    }
    NewsPaneEffects(
        cache, news, vault, feeds, articles, chrome.oldestFirst, readIds, selectedFeed?.id,
        selectedArticle, savedArticleId, folderOf,
        { thumbs -> cacheUi = cacheUi.copy(thumbs = cacheUi.thumbs + thumbs) },
        { unreadMaps = it }, onUnread, { counts = it; onUnreadCounts(it) }, { startPrefetch(null) }, ::openArticle,
    )
    fun toggleStar(article: Article) {
        if (article.id in starred) {
            starred = starred - article.id
            scope.launch { expireUnstarredBlob(cache, article.id, retainPrefs.current(), System.currentTimeMillis()) }
            return
        }
        starred = starred + article.id
        val store = vault ?: return
        scope.launch { store.upsertStar(Star(article.id, ModuleKind.News, System.currentTimeMillis())) }
    }
    fun toggleRead(article: Article) {
        val nowRead = article.id !in readIds
        readIds = if (nowRead) readIds + article.id else readIds - article.id
        vault?.let { store ->
            scope.launch {
                store.upsertReadState(ReadState(article.id, ModuleKind.News, if (nowRead) ReadStatus.Read else ReadStatus.Unread, System.currentTimeMillis()))
            }
        }
    }
    NewsPaneChrome(
        feeds, groupFeedsByFolder(feeds, folderOf), pick, selectedFolder, selectedFeed?.id, sorted,
        selectedArticle, starred, readIds, unreadMaps, statusText, refreshing, cacheUi,
        { article -> extraFeeds.firstOrNull { it.id == article.feedId }?.title.orEmpty() },
        libraryTreeOf(pick, groupFeedsByFolder(feeds, folderOf), extraFeeds, counts.news,
            counts.podcasts, counts.boards, ::applyPick),
        actions, { applyPick(LibraryPick.Folder(AppDestination.News, it)) }, ::openFeed, { refreshAll() },
        ::openArticle, ::toggleStar, ::toggleRead, ::applyPick,
        { selectedArticle = null; emitNav(selectedFolder, selectedFeed?.id, null) },
        modifier,
    )
}

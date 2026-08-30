package org.aetherfeed.app.ui.news

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.news.NewsChrome
import org.aetherfeed.app.news.NewsChromePrefs
import org.aetherfeed.app.news.clampNewsPanes
import org.aetherfeed.app.news.toggleNewsExpanded

@Composable
private fun SplitHandle(onDrag: (Float) -> Unit, onEnd: () -> Unit) {
    Box(
        Modifier
            .fillMaxHeight()
            .width(8.dp)
            .background(MaterialTheme.colorScheme.outline)
            .pointerInput(Unit) {
                detectDragGestures(onDragEnd = onEnd, onDragCancel = onEnd) { change, drag ->
                    change.consume()
                    onDrag(drag.x)
                }
            }
            .testTag("news-split"),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewsSplitLayout(
    groups: List<Pair<String, List<Feed>>>,
    selectedFolder: String?,
    selectedFeedId: String?,
    articles: List<Article>,
    selectedArticle: Article?,
    starred: Set<String>,
    readIds: Set<String>,
    unreadByFolder: Map<String, Int> = emptyMap(),
    unreadByFeed: Map<String, Int> = emptyMap(),
    statusText: String,
    feedTitleOf: (Article) -> String,
    onFolder: (String) -> Unit,
    onFeed: (Feed) -> Unit,
    onRefresh: () -> Unit,
    onOpen: (Article) -> Unit,
    onStar: (Article) -> Unit,
    onToggleRead: (Article) -> Unit,
    onBack: () -> Unit,
    stacked: Boolean,
    refreshing: Boolean,
    cache: NewsCacheUi = NewsCacheUi(),
    library: LibraryTreeState? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val prefs = remember { NewsChromePrefs(context) }
    val chrome by prefs.state.collectAsState(NewsChrome())
    val scope = rememberCoroutineScope()
    var live by remember { mutableStateOf<Triple<Float, Float, Float>?>(null) }
    val sourceW = live?.first ?: chrome.source
    val midW = live?.second ?: chrome.timeline
    val endW = live?.third ?: chrome.reader
    val nudge: (Float, Float, Float) -> Unit = { src, mid, end ->
        val panes = clampNewsPanes(src, mid, end)
        live = Triple(panes.first, panes.second, panes.third)
    }
    val commit: () -> Unit = {
        val next = live
        if (next != null) scope.launch { prefs.setWeights(next.first, next.second, next.third) }
    }
    val expandOnly: (String) -> Unit = { name ->
        scope.launch { prefs.setExpanded(toggleNewsExpanded(chrome.expanded, name)) }
    }
    val toggleFolder: (String) -> Unit = { name ->
        expandOnly(name)
        onFolder(name)
    }
    val tree: @Composable (Modifier) -> Unit = { mod ->
        if (library != null) {
            LibraryTree(
                pick = library.pick,
                newsGroups = library.newsGroups,
                podcastFeeds = library.podcastFeeds,
                boardFeeds = library.boardFeeds,
                expanded = chrome.expanded,
                unreadNews = library.unreadNews,
                unreadPodcasts = library.unreadPodcasts,
                unreadBoards = library.unreadBoards,
                unreadByFolder = unreadByFolder,
                unreadByFeed = unreadByFeed,
                onToggle = expandOnly,
                onPick = library.onPick,
                modifier = mod,
            )
        } else {
            NewsSourceTree(
                groups, selectedFolder, selectedFeedId, chrome.expanded, toggleFolder, onFeed,
                unreadByFolder, unreadByFeed, modifier = mod,
            )
        }
    }
    val timeline: @Composable (Modifier) -> Unit = { boxMod ->
        Column(boxMod) {
            NewsFilterMenu(chrome.oldestFirst) { oldest -> scope.launch { prefs.setOldestFirst(oldest) } }
            NewsReaderColumn(
                articles, selectedArticle, starred, readIds, statusText, feedTitleOf,
                onOpen, onStar, onToggleRead, showList = true, cache = cache, modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    if (stacked) {
        NewsStackedPane(
            groups = groups,
            selectedFolder = selectedFolder,
            selectedFeedId = selectedFeedId,
            expanded = chrome.expanded,
            onFolder = toggleFolder,
            onFeed = onFeed,
            unreadByFolder = unreadByFolder,
            unreadByFeed = unreadByFeed,
            articles = articles,
            selectedArticle = selectedArticle,
            starred = starred,
            readIds = readIds,
            statusText = statusText,
            feedTitleOf = feedTitleOf,
            onRefresh = onRefresh,
            onOpen = onOpen,
            onStar = onStar,
            onToggleRead = onToggleRead,
            onBack = onBack,
            onOldestFirst = { oldest -> scope.launch { prefs.setOldestFirst(oldest) } },
            oldestFirst = chrome.oldestFirst,
            refreshing = refreshing,
            cache = cache,
            library = library,
            onToggle = expandOnly,
            modifier = modifier,
        )
        return
    }
    BoxWithConstraints(modifier) {
        val total = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        Row(Modifier.fillMaxSize()) {
            tree(Modifier.fillMaxHeight().weight(sourceW))
            SplitHandle({ dx -> nudge(sourceW + dx / total, midW - dx / total, endW) }, commit)
            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxHeight().weight(midW).testTag("news-pull-refresh"),
            ) { timeline(Modifier.fillMaxHeight()) }
            SplitHandle({ dx -> nudge(sourceW, midW + dx / total, endW - dx / total) }, commit)
            NewsReaderColumn(
                articles, selectedArticle, starred, readIds, statusText, feedTitleOf,
                onOpen, onStar, onToggleRead, showList = false, cache = cache,
                modifier = Modifier.fillMaxHeight().weight(endW),
            )
        }
    }
}

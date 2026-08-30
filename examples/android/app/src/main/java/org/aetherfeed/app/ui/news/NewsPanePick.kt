package org.aetherfeed.app.ui.news

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.downloads.isUnmeteredNetwork
import org.aetherfeed.app.news.NewsRepository
import org.aetherfeed.app.news.FeedRefreshPrefs
import org.aetherfeed.app.news.canFetchNews
import org.aetherfeed.app.news.headlinesFromAll
import org.aetherfeed.app.news.prefetchUnread
import org.aetherfeed.app.news.unreadQueue
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.news.CacheRetainMode
import org.aetherfeed.app.news.dropArticleCache
import org.aetherfeed.app.news.readCachedAt
import org.aetherfeed.app.news.shouldDropCache
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.navigation.LibraryPick

internal fun articlesForPick(
    pick: LibraryPick,
    rows: List<Article>,
    extras: List<Article>,
): List<Article> = if (pick is LibraryPick.Unified) rows + extras else rows

internal fun folderFromPick(pick: LibraryPick, fallback: String?): String? = when (pick) {
    is LibraryPick.Folder -> pick.folder
    is LibraryPick.Source -> pick.folder
    else -> fallback
}

internal fun acceptPickGen(applied: Int, latest: Int): Boolean = applied == latest

internal fun CoroutineScope.startUnreadPrefetch(
    context: Context,
    refreshPrefs: FeedRefreshPrefs,
    news: NewsRepository?,
    feeds: List<org.aetherfeed.app.domain.Feed>,
    oldestFirst: Boolean,
    fromId: String?,
    readIds: Set<String>,
    cache: EncryptedCache,
    onUi: ((NewsCacheUi) -> NewsCacheUi) -> Unit,
): Job = launch {
    val allowNet = canFetchNews(refreshPrefs.currentWifiOnly(), isUnmeteredNetwork(context))
    val queue = withContext(Dispatchers.IO) {
        unreadQueue(headlinesFromAll(news, feeds, allowNet, refreshEmpty = false), oldestFirst, fromId, readIds)
    }
    prefetchUnread(queue, emptySet(), cache, allowNet) { progress ->
        withContext(Dispatchers.Main) {
            onUi { ui -> ui.withProgress(progress, progress.thumbId?.takeIf { cache.exists("articles", it) }) }
        }
    }
}

internal fun launchRefreshFeed(
    scope: CoroutineScope,
    context: Context,
    refreshPrefs: FeedRefreshPrefs,
    news: NewsRepository?,
    vault: org.aetherfeed.app.domain.LibraryRepository?,
    feed: org.aetherfeed.app.domain.Feed,
    onRefreshing: (Boolean) -> Unit,
    onStatus: (String) -> Unit,
    onArticles: (List<Article>) -> Unit,
    onRead: (String) -> Unit,
) {
    val repo = news ?: return
    onRefreshing(true)
    scope.launch {
        if (!canFetchNews(refreshPrefs.currentWifiOnly(), isUnmeteredNetwork(context))) {
            onStatus(context.getString(org.aetherfeed.app.R.string.news_wifi_blocked))
            onRefreshing(false)
            return@launch
        }
        runCatching { repo.refresh(feed.id) }
            .onSuccess { rows ->
                onArticles(rows)
                onStatus(if (rows.isEmpty()) org.aetherfeed.app.news.emptyFeedNotice(context, feed.title) else "")
                rows.forEach { article ->
                    if (vault?.readState(article.id)?.status == org.aetherfeed.app.domain.ReadStatus.Read) {
                        onRead(article.id)
                    }
                }
            }
            .onFailure { onStatus(org.aetherfeed.app.news.feedNotice(context, it, feed.title)) }
        onRefreshing(false)
    }
}

internal fun expireUnstarredBlob(
    cache: EncryptedCache,
    articleId: String,
    mode: CacheRetainMode,
    now: Long,
) {
    if (shouldDropCache(readCachedAt(cache, articleId), false, mode, now, false)) {
        dropArticleCache(cache, articleId)
    }
}

internal fun libraryTreeOf(
    pick: LibraryPick,
    grouped: List<Pair<String, List<org.aetherfeed.app.domain.Feed>>>,
    extra: List<org.aetherfeed.app.domain.Feed>,
    unreadNews: Int,
    unreadPodcasts: Int,
    unreadBoards: Int,
    onPick: (LibraryPick) -> Unit,
): LibraryTreeState = LibraryTreeState(
    pick, grouped,
    extra.filter { it.kind == org.aetherfeed.app.domain.ModuleKind.Podcast },
    extra.filter { it.kind == org.aetherfeed.app.domain.ModuleKind.Booru },
    unreadNews, unreadPodcasts, unreadBoards, onPick,
)

internal fun focusedModeFor(article: Article): AppDestination? = when {
    article.id.startsWith("podcast:") -> AppDestination.Podcasts
    article.id.startsWith("booru:") -> AppDestination.Booru
    else -> null
}

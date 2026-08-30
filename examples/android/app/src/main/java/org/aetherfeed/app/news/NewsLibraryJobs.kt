package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed

suspend fun headlinesFromAll(
    news: NewsRepository?,
    feeds: List<Feed>,
    allowNet: Boolean,
    refreshEmpty: Boolean = true,
): List<Article> {
    if (news == null) return emptyList()
    val out = ArrayList<Article>()
    for (feed in feeds) {
        var rows = runCatching { news.articles(feed.id) }.getOrDefault(emptyList())
        if (rows.isEmpty() && allowNet && refreshEmpty) {
            rows = runCatching { news.refresh(feed.id) }.getOrDefault(emptyList())
        }
        out.addAll(rows)
    }
    return out
}

data class RefreshSweep(val goneTitles: List<String> = emptyList(), val other: String = "")

suspend fun refreshAllNews(news: NewsRepository?, feeds: List<Feed>): RefreshSweep {
    if (news == null) return RefreshSweep()
    val gone = ArrayList<String>()
    var last = ""
    for (feed in feeds) {
        runCatching { news.refresh(feed.id) }.onFailure { error ->
            if (error is NewsRefreshError.Gone) gone.add(feed.title) else last = error.message.orEmpty()
        }
    }
    return RefreshSweep(gone, last)
}

data class NewsUnreadMaps(
    val total: Int = 0,
    val byFeed: Map<String, Int> = emptyMap(),
    val byFolder: Map<String, Int> = emptyMap(),
)

fun unreadByFeed(headlines: List<Article>, readIds: Set<String>): Map<String, Int> =
    headlines.groupBy { it.feedId }.mapValues { (_, rows) -> rows.count { it.id !in readIds } }.filterValues { it > 0 }

fun unreadByFolder(
    byFeed: Map<String, Int>,
    feeds: List<Feed>,
    folderOf: (Feed) -> String,
): Map<String, Int> {
    val out = HashMap<String, Int>()
    for (feed in feeds) {
        val n = byFeed[feed.id] ?: continue
        val folder = folderOf(feed)
        out[folder] = (out[folder] ?: 0) + n
    }
    return out
}

suspend fun newsUnreadMaps(
    news: NewsRepository?,
    feeds: List<Feed>,
    selected: List<Article>,
    selectedFeedId: String?,
    readIds: Set<String>,
    folderOf: (Feed) -> String,
): NewsUnreadMaps {
    val headlines = ArrayList<Article>()
    for (feed in feeds) {
        val rows = if (feed.id == selectedFeedId) {
            selected
        } else {
            news?.let { runCatching { it.articles(feed.id) }.getOrDefault(emptyList()) }.orEmpty()
        }
        headlines.addAll(rows)
    }
    val byFeed = unreadByFeed(headlines, readIds)
    return NewsUnreadMaps(byFeed.values.sum(), byFeed, unreadByFolder(byFeed, feeds, folderOf))
}

fun unreadQueue(
    headlines: List<Article>,
    oldestFirst: Boolean,
    fromId: String?,
    readIds: Set<String>,
): List<Article> = prefetchAfter(sortArticles(headlines, oldestFirst), fromId, readIds)

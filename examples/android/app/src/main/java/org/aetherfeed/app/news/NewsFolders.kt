package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Feed

const val NEWS_DEFAULT_FOLDER = "News"

fun hostOf(url: String): String {
    val host = Regex("^https?://([^/]+)").find(url)?.groupValues?.get(1).orEmpty()
    return host.removePrefix("www.")
}

fun Feed.folderLabel(): String = folder?.trim()?.ifBlank { null } ?: hostOf(url).ifBlank { NEWS_DEFAULT_FOLDER }

fun newsListEmpty(feeds: Collection<Feed>): Boolean = feeds.isEmpty()

fun groupFeedsByFolder(
    feeds: List<Feed>,
    folderOf: (Feed) -> String = { it.folderLabel() },
): List<Pair<String, List<Feed>>> =
    feeds.groupBy(folderOf).toList().sortedBy { it.first }

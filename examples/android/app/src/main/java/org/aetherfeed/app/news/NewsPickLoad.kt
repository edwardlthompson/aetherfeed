package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.navigation.LibraryPick

fun newsFeedsForPick(
    pick: LibraryPick,
    feeds: List<Feed>,
    folderOf: (Feed) -> String,
): List<Feed> {
    val newsFeeds = feeds.filter { it.kind == ModuleKind.News }
    return when (pick) {
        LibraryPick.Unified -> newsFeeds
        is LibraryPick.All -> if (pick.mode == AppDestination.News) newsFeeds else emptyList()
        is LibraryPick.Folder ->
            if (pick.mode == AppDestination.News) newsFeeds.filter { folderOf(it) == pick.folder } else emptyList()
        is LibraryPick.Source ->
            if (pick.mode == AppDestination.News) newsFeeds.filter { it.id == pick.sourceId } else emptyList()
    }
}

suspend fun headlinesForPick(
    pick: LibraryPick,
    news: NewsRepository?,
    feeds: List<Feed>,
    folderOf: (Feed) -> String,
    allowNet: Boolean,
): List<Article> = headlinesFromAll(
    news,
    newsFeedsForPick(pick, feeds, folderOf),
    allowNet,
    refreshEmpty = pick is LibraryPick.Source,
)

fun unifiedExtras(feeds: List<Feed>): List<Article> =
    feeds.filter { it.kind == ModuleKind.Podcast }.map { feed ->
        Article("podcast:${feed.id}", feed.id, feed.title, feed.url, publishedAt = feed.updatedAt)
    } + feeds.filter { it.kind == ModuleKind.Booru }.map { feed ->
        Article("booru:${feed.id}", feed.id, feed.title, feed.url.ifBlank { feed.siteUrl.orEmpty() }, publishedAt = feed.updatedAt)
    }

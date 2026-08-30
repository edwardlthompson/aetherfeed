package org.aetherfeed.app.ui.navigation

import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind

data class LibraryItem(
    val mode: AppDestination,
    val id: String,
    val title: String,
    val publishedAt: Long?,
    val url: String,
    val thumb: String? = null,
)

fun sortLibraryItems(items: List<LibraryItem>, oldestFirst: Boolean): List<LibraryItem> {
    val ordered = items.sortedWith(
        compareBy<LibraryItem> { it.publishedAt ?: Long.MAX_VALUE }.thenBy { it.id },
    )
    return if (oldestFirst) ordered else ordered.asReversed()
}

fun articlesToItems(articles: List<Article>): List<LibraryItem> =
    articles.map { LibraryItem(AppDestination.News, it.id, it.title, it.publishedAt, it.url) }

fun feedsToItems(feeds: List<Feed>, mode: AppDestination, kind: ModuleKind): List<LibraryItem> =
    feeds.filter { it.kind == kind }.map { feed ->
        LibraryItem(mode, feed.id, feed.title, feed.updatedAt, feed.url)
    }

fun mergeUnified(
    news: List<LibraryItem>,
    podcasts: List<LibraryItem>,
    boards: List<LibraryItem>,
    oldestFirst: Boolean,
): List<LibraryItem> = sortLibraryItems(news + podcasts + boards, oldestFirst)

fun shareUrl(item: LibraryItem?): String? = item?.url?.trim()?.takeIf { it.isNotEmpty() }

fun shareText(context: android.content.Context, url: String) {
    runCatching {
        val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, url)
        }
        context.startActivity(android.content.Intent.createChooser(send, null))
    }
}

fun shareUrl(articleUrl: String?, podcastUrl: String?, boardUrl: String?, mode: AppDestination): String? {
    val raw = when (mode) {
        AppDestination.News -> articleUrl
        AppDestination.Podcasts -> podcastUrl
        AppDestination.Booru -> boardUrl
    }
    return raw?.trim()?.takeIf { it.isNotEmpty() }
}

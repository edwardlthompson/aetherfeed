package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Feed

data class NewsNavState(
    val folder: String? = null,
    val feedId: String? = null,
    val articleId: String? = null,
)

fun newsCanGoBack(state: NewsNavState): Boolean =
    state.articleId != null || state.feedId != null

fun newsPop(state: NewsNavState): NewsNavState = when {
    state.articleId != null -> state.copy(articleId = null)
    state.feedId != null -> state.copy(feedId = null, articleId = null)
    else -> state
}

fun restoreNewsFeed(
    feeds: List<Feed>,
    folder: String?,
    feedId: String?,
    folderOf: (Feed) -> String,
    skip: (Feed) -> Boolean = { false },
): Feed? {
    val live = feeds.filterNot(skip).ifEmpty { feeds }
    feedId?.takeIf { it.isNotBlank() }?.let { id -> live.firstOrNull { it.id == id } }?.let { return it }
    folder?.takeIf { it.isNotBlank() }?.let { name ->
        live.firstOrNull { folderOf(it) == name }?.let { return it }
    }
    return live.firstOrNull()
}

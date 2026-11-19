package org.aetherfeed.shared.domain.notify

import org.aetherfeed.shared.domain.ModuleKind

/** Stable notification channel ids — align Android channels and iOS copy (no booru). */
object NotifyChannelIds {
    const val NEWS = "aetherfeed_news"
    const val PODCAST = "aetherfeed_podcast"
}

data class ArticleNotice(
    val channelId: String,
    val title: String,
    val body: String,
)

fun noticeChannelForModule(kind: ModuleKind): String? =
    when (kind) {
        ModuleKind.News -> NotifyChannelIds.NEWS
        ModuleKind.Podcast -> NotifyChannelIds.PODCAST
        ModuleKind.Booru -> null
    }

fun formatNewArticlesNotice(
    count: Int,
    feedTitle: String? = null,
): ArticleNotice? {
    if (count <= 0) return null
    val body =
        when {
            feedTitle.isNullOrBlank() ->
                if (count == 1) {
                    "1 new article"
                } else {
                    "$count new articles"
                }
            count == 1 -> "1 new article in $feedTitle"
            else -> "$count new articles in $feedTitle"
        }
    return ArticleNotice(NotifyChannelIds.NEWS, "AetherFeed", body)
}

fun formatNewEpisodesNotice(
    count: Int,
    showTitle: String? = null,
): ArticleNotice? {
    if (count <= 0) return null
    val body =
        when {
            showTitle.isNullOrBlank() ->
                if (count == 1) {
                    "1 new episode"
                } else {
                    "$count new episodes"
                }
            count == 1 -> "1 new episode in $showTitle"
            else -> "$count new episodes in $showTitle"
        }
    return ArticleNotice(NotifyChannelIds.PODCAST, "AetherFeed", body)
}

/** Desktop tray tooltip contract — keep aligned with `examples/desktop` tray. */
fun formatUnreadTrayLabel(count: Int): String = "AetherFeed — $count unread"
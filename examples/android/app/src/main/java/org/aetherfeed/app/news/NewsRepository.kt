package org.aetherfeed.app.news

import org.aetherfeed.app.data.defaultUnread
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository

/**
 * News boundary over [LibraryRepository]. Parsing lives in [FeedParsers];
 * this type owns subscribe/list/refresh orchestration only.
 */
interface NewsRepository {
    suspend fun listFeeds(): List<Feed>

    suspend fun subscribe(feed: Feed)

    suspend fun unsubscribe(feedId: String)

    suspend fun ingest(document: FeedDocument)
}

class DefaultNewsRepository(
    private val library: LibraryRepository,
    private val onFeedsChanged: (suspend () -> Unit)? = null,
) : NewsRepository {
    override suspend fun listFeeds(): List<Feed> =
        library.feeds().filter { it.kind == org.aetherfeed.app.domain.ModuleKind.News }

    override suspend fun subscribe(feed: Feed) {
        require(feed.kind == org.aetherfeed.app.domain.ModuleKind.News)
        library.upsertFeed(feed)
        onFeedsChanged?.invoke()
    }

    override suspend fun unsubscribe(feedId: String) {
        library.deleteFeed(feedId)
        onFeedsChanged?.invoke()
    }

    override suspend fun ingest(document: FeedDocument) {
        val nowMs = System.currentTimeMillis()
        document.feeds.forEach { library.upsertFeed(it) }
        document.articles.forEach { article ->
            library.upsertArticle(article)
            if (library.readState(article.id) == null) {
                library.upsertReadState(defaultUnread(article.id, nowMs))
            }
        }
        if (document.feeds.isNotEmpty()) {
            onFeedsChanged?.invoke()
        }
    }
}

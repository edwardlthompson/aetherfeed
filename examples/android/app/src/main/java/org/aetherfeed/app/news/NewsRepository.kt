package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed

interface NewsRepository {
    suspend fun subscribe(url: String): Feed
    suspend fun updateFeed(feed: Feed)
    suspend fun unsubscribe(feedId: String)
    suspend fun importOpml(xml: String): List<Feed>
    suspend fun exportOpml(): String
    suspend fun refresh(feedId: String): List<Article>
    suspend fun articles(feedId: String? = null): List<Article>
}

class UnimplementedNewsRepository : NewsRepository {
    override suspend fun subscribe(url: String): Feed = throw UnsupportedOperationException(url)
    override suspend fun updateFeed(feed: Feed) = throw UnsupportedOperationException(feed.id)
    override suspend fun unsubscribe(feedId: String) = throw UnsupportedOperationException(feedId)
    override suspend fun importOpml(xml: String): List<Feed> = throw UnsupportedOperationException()
    override suspend fun exportOpml(): String = throw UnsupportedOperationException()
    override suspend fun refresh(feedId: String): List<Article> = throw UnsupportedOperationException(feedId)
    override suspend fun articles(feedId: String?): List<Article> = emptyList()
}

package org.aetherfeed.app.domain

interface LibraryRepository {
    suspend fun feeds(): List<Feed>
    suspend fun upsertFeed(feed: Feed)
    suspend fun deleteFeed(id: String)

    suspend fun articles(feedId: String? = null): List<Article>
    suspend fun upsertArticle(article: Article)
    suspend fun articleBody(id: String): String?
    suspend fun cacheArticleBody(id: String, body: String)

    suspend fun readState(targetId: String): ReadState?
    suspend fun readStates(): List<ReadState>
    suspend fun upsertReadState(state: ReadState)
    suspend fun stars(): List<Star>
    suspend fun upsertStar(star: Star)
    suspend fun removeStar(targetId: String)
    suspend fun tags(): List<Tag>
    suspend fun unreadCount(): Int

    suspend fun playback(episodeId: String): PlaybackPosition?
    suspend fun playbackAll(): List<PlaybackPosition>
    suspend fun upsertPlayback(position: PlaybackPosition)
}

interface SyncProvider {
    val id: String
    suspend fun pull(): ByteArray?
    suspend fun push(blob: ByteArray)
}

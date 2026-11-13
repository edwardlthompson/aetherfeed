package org.aetherfeed.app.data

import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.PlaybackPosition
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus
import org.aetherfeed.app.domain.Star
import org.aetherfeed.app.domain.Tag
import org.aetherfeed.app.domain.totalUnread

class InMemoryLibrary : LibraryRepository {
    private val feeds = linkedMapOf<String, Feed>()
    private val articles = linkedMapOf<String, Article>()
    private val bodies = linkedMapOf<String, String>()
    private val reads = linkedMapOf<String, ReadState>()
    private val starred = linkedMapOf<String, Star>()
    private val tagged = linkedMapOf<String, Tag>()
    private val positions = linkedMapOf<String, PlaybackPosition>()

    override suspend fun feeds(): List<Feed> = feeds.values.toList()

    override suspend fun upsertFeed(feed: Feed) {
        feeds[feed.id] = feed
    }

    override suspend fun deleteFeed(id: String) {
        feeds.remove(id)
    }

    override suspend fun articles(feedId: String?): List<Article> =
        articles.values.filter { feedId == null || it.feedId == feedId }

    override suspend fun upsertArticle(article: Article) {
        articles[article.id] = article
    }

    override suspend fun articleBody(id: String): String? = bodies[id]

    override suspend fun cacheArticleBody(id: String, body: String) {
        bodies[id] = body
    }

    override suspend fun readState(targetId: String): ReadState? = reads[targetId]

    override suspend fun readStates(): List<ReadState> = reads.values.toList()

    override suspend fun upsertReadState(state: ReadState) {
        reads[state.targetId] = state
    }

    override suspend fun stars(): List<Star> = starred.values.toList()

    override suspend fun upsertStar(star: Star) {
        starred[star.targetId] = star
    }

    override suspend fun removeStar(targetId: String) {
        starred.remove(targetId)
    }

    override suspend fun tags(): List<Tag> = tagged.values.toList()

    override suspend fun unreadCount(): Int = totalUnread(reads.values)

    override suspend fun playback(episodeId: String): PlaybackPosition? = positions[episodeId]

    override suspend fun playbackAll(): List<PlaybackPosition> = positions.values.toList()

    override suspend fun upsertPlayback(position: PlaybackPosition) {
        positions[position.episodeId] = position
    }
}

fun defaultUnread(targetId: String, now: Long) = ReadState(
    targetId = targetId,
    module = org.aetherfeed.app.domain.ModuleKind.News,
    status = ReadStatus.Unread,
    // Keep initial unread at epoch so peer read receipts (Hermes) win on LWW merge.
    updatedAt = 0L,
)

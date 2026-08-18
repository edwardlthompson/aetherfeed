package org.aetherfeed.app.data

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus
import org.aetherfeed.app.domain.Star
import org.aetherfeed.app.domain.Tag
import org.aetherfeed.app.domain.totalUnread

class InMemoryLibrary : LibraryRepository {
    private val feeds = linkedMapOf<String, Feed>()
    private val reads = linkedMapOf<String, ReadState>()
    private val starred = linkedMapOf<String, Star>()
    private val tagged = linkedMapOf<String, Tag>()

    override suspend fun feeds(): List<Feed> = feeds.values.toList()

    override suspend fun upsertFeed(feed: Feed) {
        feeds[feed.id] = feed
    }

    override suspend fun deleteFeed(id: String) {
        feeds.remove(id)
    }

    override suspend fun readState(targetId: String): ReadState? = reads[targetId]

    override suspend fun upsertReadState(state: ReadState) {
        reads[state.targetId] = state
    }

    override suspend fun stars(): List<Star> = starred.values.toList()

    override suspend fun upsertStar(star: Star) {
        starred[star.targetId] = star
    }

    override suspend fun tags(): List<Tag> = tagged.values.toList()

    override suspend fun unreadCount(): Int = totalUnread(reads.values)
}

fun defaultUnread(targetId: String, now: Long) = ReadState(
    targetId = targetId,
    module = org.aetherfeed.app.domain.ModuleKind.News,
    status = ReadStatus.Unread,
    updatedAt = now,
)

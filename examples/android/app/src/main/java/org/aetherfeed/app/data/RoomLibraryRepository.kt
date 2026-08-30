package org.aetherfeed.app.data

import javax.inject.Inject
import org.aetherfeed.app.data.local.FeedEntity
import org.aetherfeed.app.data.local.LibraryDao
import org.aetherfeed.app.data.local.ReadStateEntity
import org.aetherfeed.app.data.local.StarEntity
import org.aetherfeed.app.data.local.TagEntity
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.Star
import org.aetherfeed.app.domain.Tag
import org.aetherfeed.app.domain.totalUnread

class RoomLibraryRepository @Inject constructor(
    private val dao: LibraryDao,
) : LibraryRepository {
    override suspend fun feeds(): List<Feed> = dao.feeds().map(FeedEntity::toDomain)

    override suspend fun upsertFeed(feed: Feed) {
        dao.upsertFeed(FeedEntity.from(feed))
    }

    override suspend fun deleteFeed(id: String) {
        dao.deleteFeed(id)
    }

    override suspend fun readState(targetId: String): ReadState? =
        dao.readState(targetId)?.toDomain()

    override suspend fun readStates(): List<ReadState> = dao.readStates().map { it.toDomain() }

    override suspend fun upsertReadState(state: ReadState) {
        dao.upsertReadState(ReadStateEntity.from(state))
    }

    override suspend fun stars(): List<Star> = dao.stars().map(StarEntity::toDomain)

    override suspend fun upsertStar(star: Star) {
        dao.upsertStar(StarEntity.from(star))
    }

    override suspend fun tags(): List<Tag> = dao.tags().map(TagEntity::toDomain)

    override suspend fun unreadCount(): Int = totalUnread(dao.readStates().map { it.toDomain() })
}

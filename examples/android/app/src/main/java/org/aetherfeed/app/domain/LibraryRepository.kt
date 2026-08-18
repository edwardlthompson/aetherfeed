package org.aetherfeed.app.domain

interface LibraryRepository {
    suspend fun feeds(): List<Feed>
    suspend fun upsertFeed(feed: Feed)
    suspend fun deleteFeed(id: String)

    suspend fun readState(targetId: String): ReadState?
    suspend fun upsertReadState(state: ReadState)
    suspend fun stars(): List<Star>
    suspend fun upsertStar(star: Star)
    suspend fun tags(): List<Tag>
    suspend fun unreadCount(): Int
}

interface SyncProvider {
    val id: String
    suspend fun pull(): ByteArray?
    suspend fun push(blob: ByteArray)
}

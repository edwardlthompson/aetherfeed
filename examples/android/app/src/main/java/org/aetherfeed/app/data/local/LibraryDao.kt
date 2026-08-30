package org.aetherfeed.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface LibraryDao {
    @Query("SELECT * FROM feeds")
    suspend fun feeds(): List<FeedEntity>

    @Upsert
    suspend fun upsertFeed(feed: FeedEntity)

    @Query("DELETE FROM feeds WHERE id = :id")
    suspend fun deleteFeed(id: String)

    @Query("SELECT * FROM read_state WHERE targetId = :targetId")
    suspend fun readState(targetId: String): ReadStateEntity?

    @Query("SELECT * FROM read_state")
    suspend fun readStates(): List<ReadStateEntity>

    @Upsert
    suspend fun upsertReadState(state: ReadStateEntity)

    @Query("SELECT * FROM stars")
    suspend fun stars(): List<StarEntity>

    @Upsert
    suspend fun upsertStar(star: StarEntity)

    @Query("SELECT * FROM tags")
    suspend fun tags(): List<TagEntity>

    @Upsert
    suspend fun upsertTag(tag: TagEntity)
}

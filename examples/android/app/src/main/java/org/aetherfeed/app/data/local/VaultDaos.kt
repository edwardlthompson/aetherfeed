package org.aetherfeed.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds")
    suspend fun all(): List<FeedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FeedEntity)

    @Query("DELETE FROM feeds WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE :feedId IS NULL OR feedId = :feedId")
    suspend fun byFeed(feedId: String?): List<ArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ArticleEntity)
}

@Dao
interface ArticleBodyDao {
    @Query("SELECT body FROM article_bodies WHERE id = :id")
    suspend fun get(id: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ArticleBodyEntity)
}

@Dao
interface ReadStateDao {
    @Query("SELECT * FROM read_states WHERE targetId = :targetId")
    suspend fun get(targetId: String): ReadStateEntity?

    @Query("SELECT * FROM read_states")
    suspend fun all(): List<ReadStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReadStateEntity)
}

@Dao
interface StarDao {
    @Query("SELECT * FROM stars")
    suspend fun all(): List<StarEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StarEntity)

    @Query("DELETE FROM stars WHERE targetId = :targetId")
    suspend fun delete(targetId: String)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    suspend fun all(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TagEntity)
}

@Dao
interface PlaybackDao {
    @Query("SELECT * FROM playback WHERE episodeId = :episodeId")
    suspend fun get(episodeId: String): PlaybackEntity?

    @Query("SELECT * FROM playback")
    suspend fun all(): List<PlaybackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlaybackEntity)
}

package org.aetherfeed.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FeedEntity::class,
        ArticleEntity::class,
        ArticleBodyEntity::class,
        ReadStateEntity::class,
        StarEntity::class,
        TagEntity::class,
        PlaybackEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(VaultConverters::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun feeds(): FeedDao

    abstract fun articles(): ArticleDao

    abstract fun articleBodies(): ArticleBodyDao

    abstract fun readStates(): ReadStateDao

    abstract fun stars(): StarDao

    abstract fun tags(): TagDao

    abstract fun playback(): PlaybackDao
}

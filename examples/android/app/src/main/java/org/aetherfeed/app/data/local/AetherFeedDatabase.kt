package org.aetherfeed.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FeedEntity::class, ReadStateEntity::class, StarEntity::class, TagEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(LibraryConverters::class)
abstract class AetherFeedDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
}

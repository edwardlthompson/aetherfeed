package org.aetherfeed.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadStatus

@Entity(tableName = "feeds")
data class FeedEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val kind: ModuleKind,
    val siteUrl: String?,
    val updatedAt: Long,
)

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val feedId: String,
    val title: String,
    val url: String,
    val publishedAt: Long?,
    val summary: String?,
    val localPath: String?,
)

@Entity(tableName = "article_bodies")
data class ArticleBodyEntity(
    @PrimaryKey val id: String,
    val body: String,
)

@Entity(tableName = "read_states")
data class ReadStateEntity(
    @PrimaryKey val targetId: String,
    val module: ModuleKind,
    val status: ReadStatus,
    val updatedAt: Long,
)

@Entity(tableName = "stars")
data class StarEntity(
    @PrimaryKey val targetId: String,
    val module: ModuleKind,
    val createdAt: Long,
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
)

@Entity(tableName = "playback")
data class PlaybackEntity(
    @PrimaryKey val episodeId: String,
    val positionMs: Long,
    val durationMs: Long?,
    val updatedAt: Long,
)

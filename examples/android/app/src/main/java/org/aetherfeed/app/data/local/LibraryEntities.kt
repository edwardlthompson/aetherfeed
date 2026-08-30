package org.aetherfeed.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus
import org.aetherfeed.app.domain.Star
import org.aetherfeed.app.domain.Tag

@Entity(tableName = "feeds")
data class FeedEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val kind: ModuleKind,
    val siteUrl: String?,
    val updatedAt: Long,
    val folder: String? = null,
) {
    fun toDomain(): Feed = Feed(id, title, url, kind, siteUrl, updatedAt, folder)

    companion object {
        fun from(feed: Feed) = FeedEntity(
            id = feed.id,
            title = feed.title,
            url = feed.url,
            kind = feed.kind,
            siteUrl = feed.siteUrl,
            updatedAt = feed.updatedAt,
            folder = feed.folder,
        )
    }
}

@Entity(tableName = "read_state")
data class ReadStateEntity(
    @PrimaryKey val targetId: String,
    val module: ModuleKind,
    val status: ReadStatus,
    val updatedAt: Long,
) {
    fun toDomain(): ReadState = ReadState(targetId, module, status, updatedAt)

    companion object {
        fun from(state: ReadState) = ReadStateEntity(
            targetId = state.targetId,
            module = state.module,
            status = state.status,
            updatedAt = state.updatedAt,
        )
    }
}

@Entity(tableName = "stars")
data class StarEntity(
    @PrimaryKey val targetId: String,
    val module: ModuleKind,
    val createdAt: Long,
) {
    fun toDomain(): Star = Star(targetId, module, createdAt)

    companion object {
        fun from(star: Star) = StarEntity(star.targetId, star.module, star.createdAt)
    }
}

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
) {
    fun toDomain(): Tag = Tag(id, name)

    companion object {
        fun from(tag: Tag) = TagEntity(tag.id, tag.name)
    }
}

package org.aetherfeed.app.domain

enum class ModuleKind { News, Podcast, Booru }

enum class ReadStatus { Unread, InProgress, Read }

data class Feed(
    val id: String,
    val title: String,
    val url: String,
    val kind: ModuleKind,
    val siteUrl: String? = null,
    val updatedAt: Long,
    val folder: String? = null,
)

data class Article(
    val id: String,
    val feedId: String,
    val title: String,
    val url: String,
    val publishedAt: Long? = null,
    val summary: String? = null,
    val contentHtml: String? = null,
    val localPath: String? = null,
)

data class PodcastShow(
    val id: String,
    val feedId: String,
    val title: String,
    val author: String? = null,
)

data class Episode(
    val id: String,
    val showId: String,
    val title: String,
    val enclosureUrl: String,
    val durationMs: Long? = null,
    val publishedAt: Long? = null,
    val localPath: String? = null,
)

data class BooruPost(
    val id: String,
    val sourceId: String,
    val remoteId: String,
    val fileUrl: String,
    val previewUrl: String? = null,
    val tags: List<String>,
    val localPath: String? = null,
)

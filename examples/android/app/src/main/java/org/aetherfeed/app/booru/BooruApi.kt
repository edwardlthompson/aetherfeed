package org.aetherfeed.app.booru

import org.aetherfeed.app.domain.BooruPost
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.Star

enum class BooruSourceKind { Danbooru, Gelbooru, Generic }

data class BooruSource(
    val id: String,
    val baseUrl: String,
    val kind: BooruSourceKind,
)

data class BooruSearchQuery(
    val tags: List<String>,
    val page: Int = 1,
    val limit: Int = 40,
)

interface BooruAdapter {
    fun mapPosts(rawJson: String, sourceId: String): List<BooruPost>
}

interface BooruRepository {
    suspend fun search(sourceId: String, query: BooruSearchQuery): List<BooruPost>

    suspend fun favorite(post: BooruPost)

    suspend fun blacklist(tag: String)

    suspend fun listFavorites(): List<Star>
}

/** In-memory booru state for tests and previews; production search uses [LibraryBooruRepository]. */
class InMemoryBooruRepository : BooruRepository {
    private val favorites = linkedMapOf<String, Star>()
    private val blocked = linkedSetOf<String>()

    override suspend fun search(sourceId: String, query: BooruSearchQuery): List<BooruPost> = emptyList()

    override suspend fun favorite(post: BooruPost) {
        favorites[post.id] =
            Star(targetId = post.id, module = ModuleKind.Booru, createdAt = System.currentTimeMillis())
    }

    override suspend fun blacklist(tag: String) {
        blocked += tag.lowercase()
    }

    override suspend fun listFavorites(): List<Star> = favorites.values.toList()

    fun isBlocked(tag: String): Boolean = tag.lowercase() in blocked
}

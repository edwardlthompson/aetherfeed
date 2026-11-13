package org.aetherfeed.app.booru

import org.aetherfeed.app.domain.BooruPost
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.Star

/**
 * [BooruRepository] over [LibraryRepository] for favorites; search uses injectable [BooruHttpClient].
 */
class LibraryBooruRepository(
    private val library: LibraryRepository,
    private val httpClient: BooruHttpClient,
    private val sources: Map<String, BooruSource> = emptyMap(),
    private val adapters: Map<BooruSourceKind, BooruAdapter> =
        mapOf(BooruSourceKind.Danbooru to DanbooruAdapter()),
) : BooruRepository {
    private val blocked = linkedSetOf<String>()

    override suspend fun search(sourceId: String, query: BooruSearchQuery): List<BooruPost> {
        val source = sources[sourceId] ?: return emptyList()
        val adapter = adapters[source.kind] ?: return emptyList()
        val url =
            when (source.kind) {
                BooruSourceKind.Danbooru -> DanbooruAdapter.searchUrl(source.baseUrl, query)
                BooruSourceKind.Gelbooru,
                BooruSourceKind.Generic,
                -> return emptyList()
            }
        val raw = httpClient.getBytes(url).decodeToString()
        return adapter
            .mapPosts(raw, sourceId)
            .filter { post -> post.tags.none { tag -> isBlocked(tag) } }
    }

    override suspend fun favorite(post: BooruPost) {
        library.upsertStar(
            Star(
                targetId = post.id,
                module = ModuleKind.Booru,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun blacklist(tag: String) {
        blocked += tag.lowercase()
    }

    override suspend fun listFavorites(): List<Star> =
        library.stars().filter { it.module == ModuleKind.Booru }

    fun isBlocked(tag: String): Boolean = tag.lowercase() in blocked
}

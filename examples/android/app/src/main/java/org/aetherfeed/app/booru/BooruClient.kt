package org.aetherfeed.app.booru

import org.aetherfeed.app.domain.BooruPost

enum class BooruSourceKind { Danbooru, Gelbooru, Generic }

data class BooruSource(
    val id: String,
    val kind: BooruSourceKind,
    val baseUrl: String,
    val label: String,
)

data class TagQuery(
    val tags: List<String>,
    val page: Int = 1,
)

interface BooruClient {
    suspend fun search(source: BooruSource, query: TagQuery): List<BooruPost>
    fun isBlacklisted(tag: String): Boolean
}

class LocalBooruClient(
    private val blacklist: Set<String> = emptySet(),
    private val fixtures: List<BooruPost> = emptyList(),
) : BooruClient {
    override suspend fun search(source: BooruSource, query: TagQuery): List<BooruPost> {
        val wanted = normalizeTags(query.tags)
        if (wanted.isEmpty()) return emptyList()
        if (wanted.any { isBlacklisted(it) }) return emptyList()
        return fixtures.filter { post ->
            val tags = normalizeTags(post.tags)
            wanted.all { it in tags } && tags.none { isBlacklisted(it) }
        }
    }

    override fun isBlacklisted(tag: String): Boolean = blacklist.contains(tag.trim().lowercase())

    private fun normalizeTags(tags: List<String>): List<String> =
        tags.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
}

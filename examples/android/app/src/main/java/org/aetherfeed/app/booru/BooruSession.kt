package org.aetherfeed.app.booru

import org.aetherfeed.app.domain.BooruPost

data class BoardsSnapshot(
    val posts: List<BooruPost>,
    val favorites: List<BooruPost>,
    val failed: Boolean = false,
) {
    val designedEmpty: Boolean get() = posts.isEmpty()
}

class BooruSession(
    private val client: BooruClient,
    private val source: BooruSource = localBoardsSource(),
) {
    private val favoriteOrder = linkedMapOf<String, BooruPost>()

    fun parseQuery(raw: String): TagQuery {
        val tags = raw.split(WHITESPACE).map { it.trim() }.filter { it.isNotEmpty() }
        return TagQuery(tags = tags)
    }

    suspend fun search(raw: String): BoardsSnapshot {
        val result = runCatching { client.search(source, parseQuery(raw)) }
        if (result.isFailure) {
            return BoardsSnapshot(posts = emptyList(), favorites = favorites(), failed = true)
        }
        val posts = result.getOrDefault(emptyList()).filter { post ->
            post.tags.none { client.isBlacklisted(it) }
        }
        return BoardsSnapshot(posts = posts, favorites = favorites(), failed = false)
    }

    fun toggleFavorite(post: BooruPost): Boolean {
        if (favoriteOrder.containsKey(post.id)) {
            favoriteOrder.remove(post.id)
            return false
        }
        favoriteOrder[post.id] = post
        return true
    }

    fun isFavorite(postId: String): Boolean = favoriteOrder.containsKey(postId)

    fun favorites(): List<BooruPost> = favoriteOrder.values.toList()

    companion object {
        private val WHITESPACE = Regex("\\s+")

        fun localBoardsSource(): BooruSource = BooruSource(
            id = "local",
            kind = BooruSourceKind.Generic,
            baseUrl = "https://example.invalid/boards",
            label = "Boards",
        )
    }
}

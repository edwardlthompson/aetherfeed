package org.aetherfeed.app.ui.booru

import org.aetherfeed.app.boardauth.SourceKeyStore
import org.aetherfeed.app.boarddedup.fileDigest
import org.aetherfeed.app.boardmerge.mergePosts
import org.aetherfeed.app.boardsafety.SafeModeSettings
import org.aetherfeed.app.boardsafety.allowPost
import org.aetherfeed.app.domain.BooruPost

object BooruPaneLogic {
    fun showDesignedEmpty(postCount: Int): Boolean = postCount <= 0

    fun showGrid(postCount: Int): Boolean = postCount > 0

    fun filterPosts(posts: List<BooruPost>, keys: SourceKeyStore = SourceKeyStore()): List<BooruPost> {
        val settings = SafeModeSettings()
        val seen = mutableSetOf<String>()
        val safe = posts.filter { post ->
            if (keys.requiresKey(post.sourceId) && keys.get(post.sourceId) == null) return@filter false
            allowPost(post.tags, null, settings)
        }
        return mergePosts(safe).map { it.post }.filter { post ->
            val digest = fileDigest(post.fileUrl.toByteArray())
            seen.add(digest)
        }
    }
}

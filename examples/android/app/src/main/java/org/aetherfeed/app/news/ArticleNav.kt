package org.aetherfeed.app.news

/** Next/previous index in a feed list. */
object ArticleNav {
    fun step(index: Int, size: Int, delta: Int): Int? {
        if (size <= 0) return null
        val next = index + delta
        return next.takeIf { it in 0 until size }
    }
}

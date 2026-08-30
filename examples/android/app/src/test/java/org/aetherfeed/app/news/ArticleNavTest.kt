package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Article
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleNavTest {
    private val older = Article("a1", "f1", "Old", "https://example.invalid/1", publishedAt = 10)
    private val newer = Article("a2", "f1", "New", "https://example.invalid/2", publishedAt = 20)

    @Test
    fun sortsOldestAndNewest() {
        val rows = listOf(newer, older)
        assertEquals(listOf("a1", "a2"), sortArticles(rows, oldestFirst = true).map { it.id })
        assertEquals(listOf("a2", "a1"), sortArticles(rows, oldestFirst = false).map { it.id })
    }

    @Test
    fun neighborWalksTheSortedList() {
        val rows = sortArticles(listOf(newer, older), oldestFirst = true)
        assertEquals("a2", neighborArticle(rows, "a1", 1)?.id)
        assertNull(neighborArticle(rows, "a1", -1))
        assertEquals(listOf("a1", "a2"), aroundArticles(rows, "a1").map { it.id })
        assertEquals(listOf("a1", "a2"), aroundArticles(rows, "a2").map { it.id })
    }

    @Test
    fun prefetchStartsAtSelectionThenRestOfSeries() {
        val rows = listOf(older, newer, Article("a3", "f1", "Later", "https://example.invalid/3", 30))
        assertEquals(listOf("a2", "a3"), prefetchAfter(rows, "a2", setOf("a2")).map { it.id })
        assertEquals(listOf("a1", "a2", "a3"), prefetchAfter(rows, null, emptySet()).map { it.id })
    }

    @Test
    fun unreadAmongSkipsReadIds() {
        assertEquals(1, unreadAmong(listOf("a1", "a2"), setOf("a1")))
    }

    @Test
    fun unreadQueueWalksEveryHeadline() {
        val rows = listOf(older, newer)
        assertEquals(listOf("a2", "a1"), unreadQueue(rows, oldestFirst = false, null, emptySet()).map { it.id })
    }

    @Test
    fun overlayReaderHtmlPrefersHydratedSelection() {
        val rss = older.copy(contentHtml = """<img class="webfeedsFeaturedVisual" link_thumbnail="1"><p>Teaser</p>""")
        val cached = "<p>${"Cached nerd ".repeat(40)}</p>"
        val selected = rss.copy(contentHtml = cached)
        assertEquals(cached, overlayReaderHtml(rss, selected))
        assertEquals("", overlayReaderHtml(rss, null))
        assertEquals(cached, overlayReaderHtml(rss, null, mapOf("a1" to cached)))
    }
}

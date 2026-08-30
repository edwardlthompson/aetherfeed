package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Article
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryPolicyTest {
    private fun row(id: String, publishedAt: Long?) = Article(
        id = id,
        feedId = "feed-1",
        title = id,
        url = "https://example.invalid/$id",
        publishedAt = publishedAt,
    )

    @Test
    fun defaultKeepsLastTenNewest() {
        val rows = (1..15).map { row("a$it", it.toLong()) }
        val kept = trimArticles(rows, HistoryPolicy())
        assertEquals(10, kept.size)
        assertEquals("a15", kept.first().id)
        assertEquals("a6", kept.last().id)
    }

    @Test
    fun daysModeDropsOlderAndKeepsUndated() {
        val now = 30L * 86_400_000L
        val rows = listOf(
            row("fresh", now - 2 * 86_400_000L),
            row("old", now - 20 * 86_400_000L),
            row("undated", null),
        )
        val kept = trimArticles(rows, HistoryPolicy(HistoryMode.Days, days = 7), now)
        assertEquals(listOf("fresh", "undated"), kept.map { it.id })
    }

    @Test
    fun daysClampAtThirty() {
        val now = 40L * 86_400_000L
        val rows = listOf(row("edge", now - 31 * 86_400_000L))
        val kept = trimArticles(rows, HistoryPolicy(HistoryMode.Days, days = 99), now)
        assertEquals(emptyList<Article>(), kept)
    }
}

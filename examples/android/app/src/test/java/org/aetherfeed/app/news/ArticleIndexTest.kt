package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Article
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ArticleIndexTest {
    @Test
    fun roundTripsHeadlineRows() {
        val rows = listOf(
            Article(
                id = "feed-1:1",
                feedId = "feed-1",
                title = "Hello",
                url = "https://example.invalid/1",
                publishedAt = 10L,
                summary = "Sum",
                contentHtml = "<p>Body</p>",
            ),
        )
        val decoded = decodeArticleIndex(encodeArticleIndex(rows))
        assertEquals(rows, decoded)
    }

    @Test
    fun emptyAndJunkDecodeToNone() {
        assertTrue(decodeArticleIndex("").isEmpty())
        assertTrue(decodeArticleIndex("not-json").isEmpty())
        assertTrue(decodeArticleIndex("[{}]").isEmpty())
    }
}

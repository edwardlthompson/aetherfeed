package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsArticleStoreTest {
    @Test
    fun firstDataThumbKeepsDocumentOrder() {
        val html = """<p>Lead</p><img src="data:image/jpeg;base64,AAA"><p>More</p><img src="data:image/png;base64,BBB">"""
        assertEquals("data:image/jpeg;base64,AAA", firstDataThumb(html))
    }

    @Test
    fun cacheProgressStartsAtZero() {
        val first = CacheProgress(0, 3)
        assertEquals(0, first.done)
        assertEquals(3, first.total)
        assertTrue(first.done < first.total)
        val next = CacheProgress(1, 3, "a1", "data:image/jpeg;base64,xx")
        assertEquals("a1", next.thumbId)
    }
}

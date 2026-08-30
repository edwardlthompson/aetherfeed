package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsApiTest {
    @Test
    fun flattensNestedOpmlOutlines() {
        val flat = flattenOpml(
            listOf(
                OpmlOutline(
                    title = "News",
                    children = listOf(
                        OpmlOutline(title = "Local", xmlUrl = "https://example.invalid/rss.xml"),
                        OpmlOutline(title = "Empty"),
                    ),
                ),
            ),
        )
        assertEquals(1, flat.size)
        assertEquals("https://example.invalid/rss.xml", flat[0].xmlUrl)
    }

    @Test
    fun scaffoldLeavesArticlesEmpty() {
        val repo = UnimplementedNewsRepository()
        assertTrue(kotlinx.coroutines.runBlocking { repo.articles() }.isEmpty())
    }
}

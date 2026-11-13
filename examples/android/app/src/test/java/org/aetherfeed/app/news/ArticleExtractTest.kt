package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleExtractTest {
    @Test
    fun readingBlocksKeepTextAndHttpImages() {
        val html =
            """
            <html><head><meta property="og:image" content="https://cdn.ex/hero.jpg"></head>
            <body><article>
            <p>A long enough paragraph for the extractor to keep as reading text.</p>
            <img src="https://cdn.ex/photo.jpg" alt="">
            <img src="https://cdn.ex/favicon.ico">
            </article></body></html>
            """.trimIndent()
        val blocks = ArticleExtract.blocks(html)
        val texts = blocks.filterIsInstance<ArticleBlock.Text>().map { it.value }
        val images = blocks.filterIsInstance<ArticleBlock.Image>().map { it.url }
        assertTrue(texts.any { it.contains("long enough paragraph") })
        assertTrue(images.contains("https://cdn.ex/photo.jpg"))
        assertTrue(images.none { it.contains("favicon") })
    }

    @Test
    fun fromRssStripsTags() {
        val text = ArticleExtract.fromRss("<p>Hello <b>world</b></p>", null)
        assertEquals("Hello world", text)
    }
}

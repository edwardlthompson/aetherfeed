package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleImagesTest {
    @Test
    fun collectsHttpImagesAndSkipsScripts() {
        val html = """<p><img src="https://img.example/a.png"><img src="javascript:alert(1)"></p>"""
        assertEquals(listOf("https://img.example/a.png"), collectImageSrcs(html))
        assertTrue(isFetchableImageSrc("http://img.example/b.jpg"))
        assertFalse(isFetchableImageSrc("data:image/png;base64,xx"))
    }

    @Test
    fun rewritesRemoteSrcAfterFakeFetch() {
        val html = """<img src="https://img.example/a.png">"""
        val out = rewriteImageSrcs(html, mapOf("https://img.example/a.png" to "data:image/png;base64,QQ"))
        assertTrue(out.contains("data:image/png;base64,QQ"))
        assertFalse(out.contains("https://img.example/a.png"))
        assertEquals(html, rewriteImageSrcs(html, emptyMap()))
    }

    @Test
    fun promoteLazyImagesFillsSrc() {
        val html = """<img data-src="https://cdn.example/lazy.jpg" srcset="https://cdn.example/a.jpg 320w, https://cdn.example/b.jpg 800w">"""
        val out = promoteLazyImages(html)
        assertTrue(out.contains("https://cdn.example/lazy.jpg"))
        val fromSet = promoteLazyImages("""<img srcset="https://cdn.example/a.jpg 320w, https://cdn.example/b.jpg 800w">""")
        assertTrue(fromSet.contains("https://cdn.example/b.jpg"))
    }
}

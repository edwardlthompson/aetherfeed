package org.aetherfeed.app.news

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderChromeTest {
    @Test
    fun sanitizeDropsLinksAndSiteStyle() {
        val raw = """<p style="font-family:serif"><a href="https://evil.example">Hi</a></p><script>x()</script>"""
        val out = sanitizeReaderHtml(raw)
        assertTrue(out.contains("Hi"))
        assertFalse(out.contains("href"))
        assertFalse(out.contains("evil"))
        assertFalse(out.contains("script"))
        assertFalse(out.contains("serif"))
    }

    @Test
    fun sanitizeDropsUrlsAndCommentChrome() {
        val raw = """<p>Article URL: https://ex.example/a</p><div class="comments">Talk</div><p>Story</p>"""
        val out = sanitizeReaderHtml(raw)
        assertFalse(out.contains("https://"))
        assertFalse(out.contains("Talk"))
        assertTrue(out.contains("Story"))
    }

    @Test
    fun sanitizeKeepsImageSrc() {
        val raw = """<p>Lead</p><img src="https://cdn.example/photo.jpg"><p>https://ex.example/bare</p>"""
        val out = sanitizeReaderHtml(raw)
        assertTrue(out.contains("https://cdn.example/photo.jpg"))
        assertFalse(out.contains("ex.example/bare"))
    }

    @Test
    fun wrapUsesAppColorsNotPageChrome() {
        val page = wrapReaderDocument("<p>Story</p>", "#111111", "#EEEEEE", "#888888")
        assertTrue(page.contains("background:#111111"))
        assertTrue(page.contains("color:#EEEEEE"))
        assertTrue(page.contains("font-family:sans-serif"))
        assertTrue(page.contains("Story"))
        assertTrue(page.contains("pointer-events:none"))
    }

    @Test
    fun wrapKeepsTallCachedBodyScrollable() {
        val body = "<p>${"Nerd ".repeat(800)}</p>"
        val page = wrapReaderDocument(body, "#111111", "#EEEEEE", "#888888")
        assertTrue(page.contains("Nerd"))
        assertTrue(page.contains("overflow-y:auto"))
        assertTrue(page.contains(body))
    }
}

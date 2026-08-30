package org.aetherfeed.app.news

import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.domain.Article
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleFullTextTest {
    @Test
    fun prefersLongContentHtml() = runTest {
        val html = "<p>${"Word ".repeat(80)}</p>"
        val article = Article("a1", "f1", "T", "https://ex.example/a", contentHtml = html)
        val body = resolveArticleHtml(article, { _, _ -> error("no fetch") })
        assertTrue(body.contains("Word"))
    }

    @Test
    fun teasersStayEmpty() {
        assertEquals("", usableBody("subscribe to continue"))
        assertTrue(needsFetch("short"))
        val hn = "<p>Article URL: https://ex.example/a</p><p>Comments URL: https://news.ycombinator.com/item?id=1</p>"
        assertTrue(isStubBody(hn))
        assertEquals("", usableBody(hn))
        val excerpt = """<img class="webfeedsFeaturedVisual" src="https://cdn.example/a.jpg" link_thumbnail="1"><p>${"Word ".repeat(80)}</p>"""
        assertTrue(isFeedExcerpt(excerpt))
        assertEquals("", usableBody(excerpt))
    }

    @Test
    fun extractsLongestArticleAndKeepsImageSrc() {
        val page = """
            <html><body>
              <article><p>Teaser</p></article>
              <article><p>${"Story ".repeat(90)}</p><img src="https://cdn.example/hero.jpg"></article>
            </body></html>
        """.trimIndent()
        val out = extractReadable(page)
        assertTrue(out.contains("Story"))
        assertTrue(out.contains("https://cdn.example/hero.jpg"))
        assertFalse(out.contains("Teaser"))
    }

    @Test
    fun extractsArticleFromOverlayPage() = runTest {
        val page = "<html><body><article><p>${"Story ".repeat(90)}</p></article><div>subscribe to continue</div></body></html>"
        val article = Article("a2", "f1", "T", "https://ex.example/b")
        val body = resolveArticleHtml(article, { _, _ -> page })
        assertTrue(body.contains("Story"))
    }

    @Test
    fun motoiqPagedPostUsesEntryContentAndFollowsNext() = runTest {
        val pageOne = """
            <html><head><link rel="next" href="https://motoiq.example/nerd/2/"></head>
            <body><main>
              <article class="post-pagination post-previous"><p>Tiny card</p></article>
              <article id="post-16651">
                <section class="entry-content">
                  <p>${"Nerd page one. ".repeat(30)}</p>
                  <img src="https://cdn.example/hero.jpg">
                </section>
                <div class="navigation pagination"><a href="https://motoiq.example/nerd/2/">Next page</a></div>
              </article>
            </main></body></html>
        """.trimIndent()
        val pageTwo = """
            <html><body>
              <section class="entry-content"><p>${"Nerd page two. ".repeat(30)}</p></section>
            </body></html>
        """.trimIndent()
        val extracted = extractReadable(pageOne)
        assertTrue(extracted.contains("Nerd page one"))
        assertTrue(extracted.contains("https://cdn.example/hero.jpg"))
        assertFalse(extracted.contains("Tiny card"))
        assertTrue(looksIncompletePages(pageOne))
        val article = Article("nerd", "motoiq", "Nerd", "https://motoiq.example/nerd/")
        val body = resolveArticleHtml(article, { url, _ ->
            if (url.endsWith("/2/")) pageTwo else pageOne
        })
        assertTrue(body.contains("Nerd page one"))
        assertTrue(body.contains("Nerd page two"))
    }

    @Test
    fun fetchMissKeepsUsableCache() = runTest {
        val cached = "<p>${"Cached body ".repeat(40)}</p>"
        val article = Article("a3", "f1", "T", "https://ex.example/c")
        val body = resolveArticleHtml(article, { _, _ -> error("offline") }, cached)
        assertTrue(body.contains("Cached body"))
    }

    @Test
    fun cachedUsableBodySkipsPageWalkEvenWithNextPage() = runTest {
        val cached = "<p>${"Cached nerd ".repeat(40)}</p><p>Next page</p>" +
            """<link rel="next" href="https://motoiq.example/nerd/2/">"""
        assertTrue(looksIncompletePages(cached))
        val article = Article("nerd", "motoiq", "Nerd", "https://motoiq.example/nerd/")
        val body = resolveArticleHtml(article, { _, _ -> error("no fetch") }, cached)
        assertTrue(body.contains("Cached nerd"))
    }
}

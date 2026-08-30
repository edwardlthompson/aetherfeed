package org.aetherfeed.app.news

import java.io.File
import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.applock.SessionPlainCache
import org.aetherfeed.app.domain.Article
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class NewsPrefetchSkipTest {
    private fun article(id: String): Article =
        Article(id, "f1", id, "https://example.invalid/$id")

    @Test
    fun prefetchSkipsStoredAndMarkedIds() = runTest {
        val dir = File.createTempFile("af-prefetch", ".dir").also {
            it.delete()
            it.mkdirs()
        }
        try {
            val cache = EncryptedCache(dir) { ByteArray(32) { 7 } }
            cache.write("articles", "a1", "cached".toByteArray())
            markFetched(cache, "a2")
            cache.write(
                "articles",
                "a4",
                """<img class="webfeedsFeaturedVisual" src="https://cdn.example/a.jpg" link_thumbnail="1"><p>${"Word ".repeat(80)}</p>""".toByteArray(),
            )
            markFetched(cache, "a4")
            val seen = mutableListOf<CacheProgress>()
            prefetchUnread(
                listOf(article("a1"), article("a2"), article("a3"), article("a4")),
                emptySet(),
                cache,
                allowNet = false,
            ) { seen += it }
            assertEquals(2, seen.first().total)
            assertFalse(wasFetched(cache, "a3"))
            val excerpt = """<img class="webfeedsFeaturedVisual" link_thumbnail="1"><p>${"Word ".repeat(80)}</p>"""
            val long = "<p>${"Story ".repeat(90)}</p>"
            cache.write("articles", "a5", long.toByteArray())
            val teaser = article("a5").copy(contentHtml = excerpt)
            assertFalse(needsHydrate(cache, teaser, emptySet()))
            val paged = "<p>${"Story ".repeat(90)}</p><p>Next page</p>"
            cache.write("articles", "a6", paged.toByteArray())
            assertFalse(needsHydrate(cache, article("a6"), emptySet()))
            val fetched = hydrateArticle(article("a6"), cache, allowNet = true) { _, _ -> error("no images") }
            assertTrue(fetched.html.contains("Story"))
            prefetchUnread(listOf(teaser), emptySet(), cache, allowNet = true) { seen += it }
            assertEquals(long, peekArticleHtml(cache, "a5"))
            cache.write("articles", "a7", long.toByteArray())
            markReady(cache, "a7")
            SessionPlainCache.clear()
            assertFalse(needsHydrate(cache, article("a7"), emptySet()))
            assertNull(SessionPlainCache.utf8("articles", "a7"))
        } finally {
            dir.deleteRecursively()
        }
    }
}

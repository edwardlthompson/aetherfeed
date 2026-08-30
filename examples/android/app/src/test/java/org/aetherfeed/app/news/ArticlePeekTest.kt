package org.aetherfeed.app.news

import java.io.File
import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.applock.SessionPlainCache
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.ui.news.immediateReaderHtml
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ArticlePeekTest {
    private val older = Article("a1", "f1", "Old", "https://example.invalid/1", publishedAt = 10)
    private val newer = Article("a2", "f1", "New", "https://example.invalid/2", publishedAt = 20)

    @Test
    fun peekAroundAndWaitForDisk() {
        val dir = File.createTempFile("af-peek", ".dir").also {
            it.delete()
            it.mkdirs()
        }
        try {
            SessionPlainCache.clear()
            val cache = EncryptedCache(dir) { ByteArray(32) { 3 } }
            val body = "<p>${"Cached nerd ".repeat(40)}</p>"
            cache.write("articles", "a1", body.toByteArray())
            cache.write("articles", "a2", body.replace("nerd", "next").toByteArray())
            val rows = listOf(older, newer)
            assertEquals(setOf("a1", "a2"), peekAround(cache, rows, "a1").keys)
            SessionPlainCache.clear()
            assertNull(immediateReaderHtml("a1", cache))
            assertEquals("", immediateReaderHtml("missing", cache))
            warmFeedEnds(cache, rows)
            assertTrue(SessionPlainCache.utf8("articles", "a1") != null)
        } finally {
            SessionPlainCache.clear()
            dir.deleteRecursively()
        }
    }
}

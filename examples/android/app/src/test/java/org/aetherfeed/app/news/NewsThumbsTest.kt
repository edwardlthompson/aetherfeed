package org.aetherfeed.app.news

import java.io.File
import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.domain.Article
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class NewsThumbsTest {
    private fun cacheDir(): File =
        File.createTempFile("af-thumbs", ".dir").also {
            it.delete()
            it.mkdirs()
        }

    private fun article(id: String): Article = Article(id, "f1", id, "https://example.invalid/$id")

    @Test
    fun loadThumbReturnsStoredBytesWithoutArticleBody() {
        val dir = cacheDir()
        try {
            val cache = EncryptedCache(dir) { ByteArray(32) { 7 } }
            cache.write(THUMB_KIND, "a1", "data:image/jpeg;base64,abc".toByteArray())
            cache.write("articles", "a1", "<p>${"Huge ".repeat(200)}<img src=\"data:image/jpeg;base64,BODY\"></p>".toByteArray())
            assertEquals("data:image/jpeg;base64,abc", loadThumb(cache, "a1"))
            assertNull(loadThumb(cache, "missing"))
            val bulk = loadThumbs(cache, listOf("a1", "missing", "a1"))
            assertEquals(mapOf("a1" to "data:image/jpeg;base64,abc"), bulk)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun hydrateUsesStoredThumbAndSkipsImageFetch() = runTest {
        val dir = cacheDir()
        try {
            val cache = EncryptedCache(dir) { ByteArray(32) { 7 } }
            cache.write(THUMB_KIND, "a6", "data:image/jpeg;base64,STORED".toByteArray())
            cache.write("articles", "a6", "<p>${"Story ".repeat(90)}</p><img src=\"data:image/jpeg;base64,BODY\">".toByteArray())
            val fetched = hydrateArticle(article("a6"), cache, allowNet = true) { _, _ -> error("no images") }
            assertTrue(fetched.html.contains("Story"))
            assertEquals("data:image/jpeg;base64,STORED", fetched.thumb)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun loadHeadlineThumbsReadsVisibleIds() {
        val dir = cacheDir()
        try {
            val cache = EncryptedCache(dir) { ByteArray(32) { 7 } }
            cache.write(THUMB_KIND, "a1", "data:image/jpeg;base64,aa".toByteArray())
            val loaded = loadHeadlineThumbs(cache, listOf(article("a1"), article("a2")))
            assertEquals("data:image/jpeg;base64,aa", loaded["a1"])
            assertNull(loaded["a2"])
        } finally {
            dir.deleteRecursively()
        }
    }
}

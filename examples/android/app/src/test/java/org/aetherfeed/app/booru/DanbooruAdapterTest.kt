package org.aetherfeed.app.booru

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class DanbooruAdapterTest {
    private val adapter = DanbooruAdapter()

    @Test
    fun mapsFixturePostsAndTags() {
        val json =
            checkNotNull(javaClass.getResourceAsStream("/danbooru_posts.json"))
                .use { it.readBytes().toString(StandardCharsets.UTF_8) }
        val posts = adapter.mapPosts(json, sourceId = "fixture")
        assertEquals(2, posts.size)
        val first = posts[0]
        assertEquals("fixture:42", first.id)
        assertEquals("42", first.remoteId)
        assertTrue(first.tags.contains("scenery"))
        assertEquals("https://example.test/previews/42.jpg", first.previewUrl)
        val second = posts[1]
        assertEquals("https://example.test/files/43.png", second.fileUrl)
        assertTrue(second.tags.contains("abstract"))
    }

    @Test
    fun buildsTagSearchUrl() {
        val url =
            DanbooruAdapter.searchUrl(
                baseUrl = "https://danbooru.example",
                query = BooruSearchQuery(tags = listOf("landscape", "safe"), page = 2, limit = 20),
            )
        assertEquals(
            "https://danbooru.example/posts.json?tags=landscape+safe&page=2&limit=20",
            url,
        )
    }
}

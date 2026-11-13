package org.aetherfeed.app.booru

import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.data.InMemoryLibrary
import org.aetherfeed.app.domain.ModuleKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class LibraryBooruRepositoryTest {
    private val source =
        BooruSource(
            id = "test-dan",
            baseUrl = "https://booru.example",
            kind = BooruSourceKind.Danbooru,
        )

    private val fixture =
        """
        [
          {
            "id": 7,
            "tag_string": "cat safe",
            "file_url": "https://example.test/7.jpg"
          }
        ]
        """.trimIndent().toByteArray(StandardCharsets.UTF_8)

    @Test
    fun searchParsesFixtureWithoutNetwork() =
        runBlocking {
            val query = BooruSearchQuery(tags = listOf("cat"), page = 1, limit = 5)
            val url = DanbooruAdapter.searchUrl(source.baseUrl, query)
            val library = InMemoryLibrary()
            val repo =
                LibraryBooruRepository(
                    library = library,
                    httpClient = MapBooruHttpClient(mapOf(url to fixture)),
                    sources = mapOf(source.id to source),
                )
            val posts = repo.search(source.id, query)
            assertEquals(1, posts.size)
            assertEquals("test-dan:7", posts[0].id)
            assertEquals(listOf("cat", "safe"), posts[0].tags)
        }

    @Test
    fun favoritePersistsInLibrary() =
        runBlocking {
            val library = InMemoryLibrary()
            val repo =
                LibraryBooruRepository(
                    library = library,
                    httpClient = MapBooruHttpClient(emptyMap()),
                    sources = emptyMap(),
                )
            val post =
                checkNotNull(
                    DanbooruAdapter().mapPosts(fixture.decodeToString(), source.id).single(),
                )
            repo.favorite(post)
            val stars = repo.listFavorites()
            assertEquals(1, stars.size)
            assertEquals(ModuleKind.Booru, stars[0].module)
            assertEquals(post.id, stars[0].targetId)
        }

    @Test
    fun blacklistFiltersTagMatches() =
        runBlocking {
            val query = BooruSearchQuery(tags = listOf("cat"), page = 1, limit = 5)
            val url = DanbooruAdapter.searchUrl(source.baseUrl, query)
            val library = InMemoryLibrary()
            val repo =
                LibraryBooruRepository(
                    library = library,
                    httpClient = MapBooruHttpClient(mapOf(url to fixture)),
                    sources = mapOf(source.id to source),
                )
            repo.blacklist("cat")
            val posts = repo.search(source.id, query)
            assertTrue(posts.isEmpty())
        }
}

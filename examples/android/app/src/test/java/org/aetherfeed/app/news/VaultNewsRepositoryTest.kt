package org.aetherfeed.app.news

import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.data.InMemoryLibrary
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class VaultNewsRepositoryTest {
    private fun feed(): Feed = Feed(
        id = "feed-1",
        title = "Local",
        url = "https://example.invalid/feed.json",
        kind = ModuleKind.News,
        updatedAt = 1,
    )

    @Test
    fun refreshAbortIsTypedAndLeavesArticlesEmpty() = runTest {
        val library = InMemoryLibrary()
        library.upsertFeed(feed())
        val repo = VaultNewsRepository(library, FeedBodyFetcher { _, _ -> throw NewsRefreshError.Aborted() })
        val error = runCatching { repo.refresh("feed-1") }.exceptionOrNull()
        assertTrue(error is NewsRefreshError.Aborted)
        assertTrue(repo.articles().isEmpty())
    }

    @Test
    fun refreshCancelMapsToAborted() = runTest {
        val library = InMemoryLibrary()
        library.upsertFeed(feed())
        val repo = VaultNewsRepository(library, FeedBodyFetcher { _, _ -> throw CancellationException("gone") })
        val error = runCatching { repo.refresh("feed-1") }.exceptionOrNull()
        assertTrue(error is NewsRefreshError.Aborted)
        assertTrue(repo.articles().isEmpty())
    }

    @Test
    fun refreshTimeoutIsTyped() = runTest {
        val library = InMemoryLibrary()
        library.upsertFeed(feed())
        val repo = VaultNewsRepository(library, FeedBodyFetcher { _, _ -> throw SocketTimeoutException("slow") })
        val error = runCatching { repo.refresh("feed-1") }.exceptionOrNull()
        assertTrue(error is NewsRefreshError.Timeout)
    }

    @Test
    fun articlesEmptyBeforeRefresh() = runTest {
        val repo = VaultNewsRepository(InMemoryLibrary(), FeedBodyFetcher { _, _ -> "" })
        assertTrue(repo.articles().isEmpty())
        assertTrue(newsListEmpty(emptyList()))
    }

    @Test
    fun refreshMapsJsonFeedItemsUnread() = runTest {
        val library = InMemoryLibrary()
        library.upsertFeed(feed())
        val body = """{"version":"https://jsonfeed.org/version/1.1","title":"Local",
            "items":[{"id":"1","title":"Hello","url":"https://example.invalid/1"}]}"""
        val repo = VaultNewsRepository(library, FeedBodyFetcher { _, _ -> body })
        val rows = repo.refresh("feed-1")
        assertEquals(1, rows.size)
        assertEquals("Hello", rows.single().title)
        assertEquals(ReadStatus.Unread, library.readState(rows.single().id)?.status)
    }

    @Test
    fun refreshTrimsToDefaultTen() = runTest {
        val library = InMemoryLibrary()
        library.upsertFeed(feed())
        val items = (1..15).joinToString(",") { id ->
            """{"id":"$id","title":"T$id","url":"https://example.invalid/$id"}"""
        }
        val body = """{"version":"https://jsonfeed.org/version/1.1","title":"Local","items":[$items]}"""
        val repo = VaultNewsRepository(library, FeedBodyFetcher { _, _ -> body })
        assertEquals(10, repo.refresh("feed-1").size)
        assertEquals(10, repo.articles("feed-1").size)
    }

    @Test
    fun articlesHydrateFromPersistedIndex() = runTest {
        val store = object : ArticleIndexStore {
            private val rows = mutableMapOf<String, List<Article>>()
            override fun load(feedId: String) = rows[feedId].orEmpty()
            override fun save(feedId: String, rows: List<Article>) {
                this.rows[feedId] = rows
            }
        }
        store.save(
            "feed-1",
            listOf(
                Article(
                    id = "feed-1:1",
                    feedId = "feed-1",
                    title = "Cached",
                    url = "https://example.invalid/1",
                ),
            ),
        )
        val repo = VaultNewsRepository(
            InMemoryLibrary(),
            FeedBodyFetcher { _, _ -> error("no fetch") },
            index = store,
        )
        assertEquals("Cached", repo.articles("feed-1").single().title)
    }
}

package org.aetherfeed.app.news

import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.data.InMemoryLibrary
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NewsRepositoryTest {
    @Test
    fun ingestCreatesUnreadForNewArticles() =
        runBlocking {
            val library = InMemoryLibrary()
            val repo = DefaultNewsRepository(library)
            val feed =
                Feed(
                    id = "https://example.test/feed",
                    title = "Demo",
                    url = "https://example.test/feed",
                    kind = ModuleKind.News,
                    updatedAt = 1L,
                )
            val article =
                Article(
                    id = "a1",
                    feedId = feed.id,
                    title = "Post",
                    url = "https://example.test/a1",
                )
            repo.ingest(FeedDocument(feeds = listOf(feed), articles = listOf(article)))
            val state = library.readState("a1")
            assertNotNull(state)
            assertEquals(ReadStatus.Unread, state?.status)
            assertEquals(ModuleKind.News, state?.module)
            assertEquals(1, library.unreadCount())
        }
}

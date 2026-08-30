package org.aetherfeed.app.news

import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.junit.Assert.assertEquals
import org.junit.Test

class NewsLibraryJobsTest {
    private class CountingRepo(
        var stored: List<Article>,
    ) : NewsRepository {
        var refreshes = 0
        var failWith: Throwable? = null

        override suspend fun subscribe(url: String) = error("unused")
        override suspend fun updateFeed(feed: Feed) = error("unused")
        override suspend fun unsubscribe(feedId: String) = error("unused")
        override suspend fun importOpml(xml: String) = error("unused")
        override suspend fun exportOpml() = error("unused")

        override suspend fun articles(feedId: String?) =
            stored.filter { feedId == null || it.feedId == feedId }

        override suspend fun refresh(feedId: String): List<Article> {
            refreshes += 1
            failWith?.let { throw it }
            return stored.filter { it.feedId == feedId }
        }
    }

    private fun feed(id: String = "f1"): Feed =
        Feed(id, "Local", "https://example.invalid/rss.xml", ModuleKind.News, updatedAt = 1)

    private fun article(id: String, feedId: String = "f1"): Article =
        Article(id, feedId, id, "https://example.invalid/$id")

    @Test
    fun headlinesSkipRefreshWhenIndexHasRows() = runTest {
        val news = CountingRepo(listOf(article("a1")))
        val rows = headlinesFromAll(news, listOf(feed()), allowNet = true)
        assertEquals(listOf("a1"), rows.map { it.id })
        assertEquals(0, news.refreshes)
    }

    @Test
    fun refreshAllCollectsGoneTitles() = runTest {
        val news = CountingRepo(emptyList())
        news.failWith = NewsRefreshError.Gone("HTTP 404")
        val sweep = refreshAllNews(
            news,
            listOf(
                feed("f1").copy(title = "Dead One"),
                feed("f2").copy(title = "Dead Two"),
            ),
        )
        assertEquals(listOf("Dead One", "Dead Two"), sweep.goneTitles)
        assertEquals("", sweep.other)
    }

    @Test
    fun headlinesRefreshEmptyIndexesOnce() = runTest {
        val news = CountingRepo(emptyList())
        headlinesFromAll(news, listOf(feed()), allowNet = true)
        assertEquals(1, news.refreshes)
        news.refreshes = 0
        headlinesFromAll(news, listOf(feed()), allowNet = true, refreshEmpty = false)
        assertEquals(0, news.refreshes)
    }

    @Test
    fun unreadMapsHideZeros() {
        val feeds = listOf(
            feed("f1").copy(folder = "World"),
            feed("f2").copy(folder = "World"),
            feed("f3").copy(folder = "Art"),
        )
        val headlines = listOf(article("a1", "f1"), article("a2", "f1"), article("b1", "f2"), article("c1", "f3"))
        val byFeed = unreadByFeed(headlines, setOf("a1", "c1"))
        assertEquals(mapOf("f1" to 1, "f2" to 1), byFeed)
        assertEquals(mapOf("World" to 2), unreadByFolder(byFeed, feeds) { it.folder ?: "" })
    }
}

package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsNavTest {
    @Test
    fun popsArticleThenFeedAndDoesNotExit() {
        val article = NewsNavState(folder = "World", feedId = "f1", articleId = "a1")
        assertTrue(newsCanGoBack(article))
        val timeline = newsPop(article)
        assertEquals("f1", timeline.feedId)
        assertEquals(null, timeline.articleId)
        assertTrue(newsCanGoBack(timeline))
        val folder = newsPop(timeline)
        assertEquals("World", folder.folder)
        assertEquals(null, folder.feedId)
        assertFalse(newsCanGoBack(folder))
        assertEquals(folder, newsPop(folder))
    }

    @Test
    fun restorePrefersSavedFeedThenFolder() {
        val cars = Feed("feed:cars", "Cars", "https://ex.invalid/c", ModuleKind.News, updatedAt = 1, folder = "Automotive")
        val world = Feed("feed:world", "World", "https://ex.invalid/w", ModuleKind.News, updatedAt = 1, folder = "World")
        val folderOf: (Feed) -> String = { it.folder ?: "" }
        assertEquals("feed:world", restoreNewsFeed(listOf(cars, world), "Automotive", "feed:world", folderOf)?.id)
        assertEquals("feed:cars", restoreNewsFeed(listOf(cars, world), "Automotive", "gone", folderOf)?.id)
        assertEquals("feed:cars", restoreNewsFeed(listOf(cars, world), null, null, folderOf)?.id)
    }
}

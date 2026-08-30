package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsFoldersTest {
    @Test
    fun emptyFeedsMeansEmptyList() {
        assertTrue(newsListEmpty(emptyList()))
        assertFalse(
            newsListEmpty(
                listOf(Feed("1", "A", "https://a.example/rss.xml", ModuleKind.News, updatedAt = 1)),
            ),
        )
    }

    @Test
    fun groupsByHostAsFolder() {
        val feeds = listOf(
            Feed("1", "A", "https://news.example/a.xml", ModuleKind.News, updatedAt = 1),
            Feed("2", "B", "https://news.example/b.xml", ModuleKind.News, updatedAt = 1),
        )
        val groups = groupFeedsByFolder(feeds)
        assertEquals(1, groups.size)
        assertEquals("news.example", groups.single().first)
        assertEquals(2, groups.single().second.size)
    }

    @Test
    fun prefersStoredFolder() {
        val feed = Feed("1", "A", "https://news.example/a.xml", ModuleKind.News, updatedAt = 1, folder = "Art")
        assertEquals("Art", feed.folderLabel())
    }
}

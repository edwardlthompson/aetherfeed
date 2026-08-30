package org.aetherfeed.app.ui.navigation

import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class LibraryPickTest {
    @Test
    fun encodeRoundTripAndParent() {
        val folder = LibraryPick.Folder(AppDestination.News, "Automotive")
        val source = LibraryPick.Source(AppDestination.News, "Automotive", "moto")
        assertEquals(folder, parseLibraryPick(encodeLibraryPick(folder)))
        assertEquals(source, parseLibraryPick(encodeLibraryPick(source)))
        assertEquals(LibraryPick.Unified, parseLibraryPick("unified"))
        assertEquals(LibraryPick.All(AppDestination.News), folder.parent())
        assertEquals(folder, source.parent())
        assertNull(LibraryPick.All(AppDestination.News).parent())
        assertEquals(3, modeTabDestinations().size)
        assertTrue(AppDestination.entries.none { it.name == "Unified" })
    }

    @Test
    fun sortAndMergeUnified() {
        val news = articlesToItems(
            listOf(Article("a1", "f1", "Old", "https://n.example/1", publishedAt = 10)),
        )
        val pods = feedsToItems(
            listOf(Feed("p1", "Show", "https://p.example/rss", ModuleKind.Podcast, updatedAt = 20)),
            AppDestination.Podcasts,
            ModuleKind.Podcast,
        )
        val boards = feedsToItems(
            listOf(Feed("b1", "Board", "https://b.example/api", ModuleKind.Booru, updatedAt = 0)),
            AppDestination.Booru,
            ModuleKind.Booru,
        )
        val newest = mergeUnified(news, pods, boards, oldestFirst = false)
        assertEquals(listOf("p1", "a1", "b1"), newest.map { it.id })
        assertEquals("https://n.example/1", shareUrl(newest.first { it.id == "a1" }))
        assertNull(shareUrl(LibraryItem(AppDestination.News, "x", "X", 1, "  ")))
        assertTrue(mergeUnified(emptyList(), emptyList(), emptyList(), false).isEmpty())
    }

    @Test
    fun folderPickScopesNewsFeedsAndLastGenWins() {
        val auto = Feed("m", "Moto", "https://m.example/rss", ModuleKind.News, updatedAt = 1, folder = "Automotive")
        val other = Feed("z", "Other", "https://z.example/rss", ModuleKind.News, updatedAt = 1, folder = "Games")
        val scoped = org.aetherfeed.app.news.newsFeedsForPick(
            LibraryPick.Folder(AppDestination.News, "Automotive"),
            listOf(auto, other),
            { feed -> feed.folder.orEmpty() },
        )
        assertEquals(listOf("m"), scoped.map { it.id })
        assertTrue(org.aetherfeed.app.ui.news.acceptPickGen(2, 2))
        assertTrue(!org.aetherfeed.app.ui.news.acceptPickGen(1, 2))
    }
}

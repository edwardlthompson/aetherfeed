package org.aetherfeed.app.ui.navigation

import org.aetherfeed.app.R
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class ModeDestinationsTest {
    @Test
    fun modeTabsAreNewsPodcastsBoards() {
        val tabs = modeTabDestinations()
        assertEquals(listOf(AppDestination.News, AppDestination.Podcasts, AppDestination.Booru), tabs)
        assertTrue(tabs.all { it.isModeDestination() })
        assertTrue(tabs.none { it.name == "Settings" })
        assertEquals(R.string.nav_booru, AppDestination.Booru.labelRes)
        assertEquals(R.string.pane_booru_body, boardsEmptyBodyRes())
    }

    @Test
    fun boardsEmptyChromeWhenZeroSources() {
        assertTrue(shouldShowBoardsEmpty(0))
        assertFalse(shouldShowBoardsEmpty(1))
        assertEquals("boards-empty", BOARDS_EMPTY_TEST_TAG)
        assertEquals(0, countBoardSources(emptyList()))
        assertEquals(
            1,
            countBoardSources(
                listOf(
                    Feed("n", "News", "https://n.example/rss", ModuleKind.News, updatedAt = 1),
                    Feed("b", "Board", "https://b.example/api", ModuleKind.Booru, updatedAt = 1),
                ),
            ),
        )
    }
}

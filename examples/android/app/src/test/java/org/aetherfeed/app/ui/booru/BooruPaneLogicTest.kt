package org.aetherfeed.app.ui.booru

import org.aetherfeed.app.domain.BooruPost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BooruPaneLogicTest {
    @Test
    fun designedEmptyWhenGridHasNoPosts() {
        assertTrue(BooruPaneLogic.showDesignedEmpty(0))
        assertTrue(BooruPaneLogic.showDesignedEmpty(-1))
        assertFalse(BooruPaneLogic.showGrid(0))
    }

    @Test
    fun gridWhenSearchReturnsPosts() {
        assertFalse(BooruPaneLogic.showDesignedEmpty(2))
        assertTrue(BooruPaneLogic.showGrid(2))
    }

    @Test
    fun filtersUnsafeAndDuplicatePosts() {
        val posts = listOf(
            BooruPost("1", "local", "1", "https://x/a.jpg", tags = listOf("sky")),
            BooruPost("2", "local", "2", "https://x/a.jpg", tags = listOf("sky")),
            BooruPost("3", "local", "3", "https://x/b.jpg", tags = listOf("rating:e")),
        )
        val kept = BooruPaneLogic.filterPosts(posts)
        assertEquals(1, kept.size)
        assertEquals("1", kept.first().id)
    }
}

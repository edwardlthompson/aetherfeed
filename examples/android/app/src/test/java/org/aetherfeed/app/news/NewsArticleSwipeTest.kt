package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Test

class NewsArticleSwipeTest {
    @Test
    fun leftEdgeBelongsToFolders() {
        assertEquals(0, articleSwipePageDelta(20f, -280f, -3000f, 360f, 48f, 112f))
    }

    @Test
    fun smallMovesDoNotPage() {
        assertEquals(0, articleSwipePageDelta(80f, -40f, -400f, 360f, 48f, 112f))
        assertEquals(0, articleSwipePageDelta(80f, -160f, -800f, 360f, 48f, 112f))
    }

    @Test
    fun grandTravelOrFastFlingPages() {
        assertEquals(1, articleSwipePageDelta(80f, -280f, 0f, 360f, 48f, 112f))
        assertEquals(-1, articleSwipePageDelta(80f, 280f, 0f, 360f, 48f, 112f))
        assertEquals(1, articleSwipePageDelta(80f, -120f, -2400f, 360f, 48f, 112f))
    }

    @Test
    fun pagerEatsSlopThenYields() {
        assertEquals(-20f, newsPagerConsumeX(80f, 0f, -20f, 48f, 112f))
        assertEquals(0f, newsPagerConsumeX(80f, -112f, -20f, 48f, 112f))
        assertEquals(-20f, newsPagerConsumeX(10f, 0f, -20f, 48f, 112f))
    }

    @Test
    fun verticalDragLocksReaderAndEatsX() {
        assertEquals(0, newsPagerAxis(4f, 8f, 16f))
        assertEquals(-1, newsPagerAxis(10f, 80f, 16f))
        assertEquals(1, newsPagerAxis(80f, 10f, 16f))
        assertEquals(0, articleSwipePageDelta(80f, -40f, 0f, 360f, 48f, 112f))
        assertEquals(-40f, newsPagerPreScrollX(-1, 80f, 0f, -40f, 48f, 112f))
        assertEquals(-20f, newsPagerPreScrollX(0, 80f, 0f, -20f, 48f, 112f))
        assertEquals(0f, newsPagerPreScrollX(1, 80f, -112f, -20f, 48f, 112f))
    }
}

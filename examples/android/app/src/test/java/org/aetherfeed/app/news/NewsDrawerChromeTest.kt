package org.aetherfeed.app.news

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsDrawerChromeTest {
    @Test
    fun closedDrawerIsOffstage() {
        assertFalse(newsDrawerPainted(open = false, dragPx = 0f))
        assertFalse(newsDrawerPainted(open = false, dragPx = 0.4f))
    }

    @Test
    fun openOrDraggingPaintsTheDrawer() {
        assertTrue(newsDrawerPainted(open = true, dragPx = 0f))
        assertTrue(newsDrawerPainted(open = false, dragPx = 24f))
    }

    @Test
    fun settleRequiresARealPull() {
        assertFalse(newsDrawerSettleOpen(40f, 304f))
        assertTrue(newsDrawerSettleOpen(120f, 304f))
    }
}

package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewsRefreshRateTest {
    @Test
    fun emptyModesYieldNothing() {
        assertNull(pickNewsRefreshMode(emptyList(), 1080, 2400))
    }

    @Test
    fun prefersHighestRateAtCurrentResolution() {
        val modes = listOf(
            NewsDisplayMode(1, 1080, 2400, 60f),
            NewsDisplayMode(2, 1080, 2400, 120f),
            NewsDisplayMode(3, 720, 1600, 144f),
        )
        val picked = pickNewsRefreshMode(modes, 1080, 2400)
        assertEquals(2, picked?.id)
        assertEquals(120f, picked?.hz)
    }

    @Test
    fun idleDropsRateAndMode() {
        assertEquals(0f, newsRefreshHz(false, 120f))
        assertEquals(0, newsRefreshModeId(false, 2))
        assertEquals(120f, newsRefreshHz(true, 120f))
        assertEquals(2, newsRefreshModeId(true, 2))
    }
}

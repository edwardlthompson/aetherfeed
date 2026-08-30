package org.aetherfeed.app.news

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsNetworkTest {
    @Test
    fun wifiOnlyBlocksMetered() {
        assertFalse(canFetchNews(wifiOnly = true, unmetered = false))
        assertTrue(canFetchNews(wifiOnly = true, unmetered = true))
        assertTrue(canFetchNews(wifiOnly = false, unmetered = false))
    }
}

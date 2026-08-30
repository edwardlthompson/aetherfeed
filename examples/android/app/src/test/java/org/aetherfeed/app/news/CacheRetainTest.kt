package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CacheRetainTest {
    private val now = 1_700_000_000_000L

    @Test
    fun starredNeverDrops() {
        assertFalse(
            shouldDropCache(now - CACHE_RETAIN_MS - 1, starred = true, CacheRetainMode.Days30, now, true),
        )
        assertFalse(
            shouldDropCache(now - 1, starred = true, CacheRetainMode.NextSync, now, true),
        )
    }

    @Test
    fun days30AndNextSync() {
        assertTrue(shouldDropCache(now - CACHE_RETAIN_MS, starred = false, CacheRetainMode.Days30, now, false))
        assertFalse(shouldDropCache(now - 1_000, starred = false, CacheRetainMode.Days30, now, true))
        assertTrue(shouldDropCache(now, starred = false, CacheRetainMode.NextSync, now, true))
        assertFalse(shouldDropCache(now, starred = false, CacheRetainMode.NextSync, now, false))
        assertFalse(shouldDropCache(null, starred = false, CacheRetainMode.Days30, now, true))
        assertEquals(CacheRetainMode.NextSync, parseCacheRetainMode("sync"))
        assertEquals("days30", encodeCacheRetainMode(CacheRetainMode.Days30))
    }
}

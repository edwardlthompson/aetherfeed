package org.aetherfeed.app.news

import org.junit.Assert.assertTrue
import org.junit.Test

class NewsRefreshErrorTest {
    @Test
    fun http404And410AreGone() {
        assertTrue(httpRefreshError(404) is NewsRefreshError.Gone)
        assertTrue(httpRefreshError(410) is NewsRefreshError.Gone)
        assertTrue(httpRefreshError(500) is NewsRefreshError.Unavailable)
        assertTrue(httpRefreshError(403) is NewsRefreshError.Unavailable)
    }
}

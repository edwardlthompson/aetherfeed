package org.aetherfeed.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class AppDestinationTest {
    @Test
    fun threeModeTabsWithoutSettings() {
        assertEquals(3, AppDestination.entries.size)
        assertTrue(AppDestination.entries.none { it.name == "Settings" })
        assertEquals(AppDestination.News, AppDestination.entries.first())
        assertEquals(AppDestination.Booru, AppDestination.entries.last())
        assertEquals(AppDestination.Podcasts, parseAppDestination("podcasts"))
        assertEquals(AppDestination.News, parseAppDestination(null))
    }
}

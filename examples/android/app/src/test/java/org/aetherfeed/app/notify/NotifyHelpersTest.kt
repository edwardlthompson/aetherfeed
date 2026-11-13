package org.aetherfeed.app.notify

import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.NotificationChannelPref
import org.junit.Assert.assertEquals
import org.junit.Test

class NotifyHelpersTest {
    @Test
    fun enabledChannelIdsRespectsEnabledFlag() {
        val prefs =
            listOf(
                NotificationChannelPref(NotifyChannelIds.NEWS, ModuleKind.News, true, "News"),
                NotificationChannelPref(NotifyChannelIds.PODCAST, ModuleKind.Podcast, false, "Podcasts"),
            )
        assertEquals(listOf(NotifyChannelIds.NEWS), enabledChannelIds(prefs))
    }

    @Test
    fun defaultChannelIdConstants() {
        assertEquals("aetherfeed_news", NotifyChannelIds.NEWS)
        assertEquals("aetherfeed_podcast", NotifyChannelIds.PODCAST)
    }

    @Test
    fun formatUnreadTrayLabelMatchesDesktopContract() {
        assertEquals("AetherFeed — 0 unread", formatUnreadTrayLabel(0))
        assertEquals("AetherFeed — 12 unread", formatUnreadTrayLabel(12))
    }
}

package org.aetherfeed.shared.domain.notify

import org.aetherfeed.shared.domain.ModuleKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotifyContractsTest {
    @Test
    fun noticeChannelSkipsBooru() {
        assertEquals(NotifyChannelIds.NEWS, noticeChannelForModule(ModuleKind.News))
        assertEquals(NotifyChannelIds.PODCAST, noticeChannelForModule(ModuleKind.Podcast))
        assertNull(noticeChannelForModule(ModuleKind.Booru))
    }

    @Test
    fun formatNewArticlesNotice() {
        assertNull(formatNewArticlesNotice(0))
        assertEquals(
            "1 new article",
            formatNewArticlesNotice(1)?.body,
        )
        assertEquals(
            "3 new articles in Tech",
            formatNewArticlesNotice(3, "Tech")?.body,
        )
        assertEquals(NotifyChannelIds.NEWS, formatNewArticlesNotice(1)?.channelId)
    }

    @Test
    fun formatUnreadTrayLabelMatchesDesktop() {
        assertEquals("AetherFeed — 0 unread", formatUnreadTrayLabel(0))
        assertEquals("AetherFeed — 12 unread", formatUnreadTrayLabel(12))
    }
}
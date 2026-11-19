package org.aetherfeed.desktop.unread

import org.aetherfeed.shared.domain.ReadStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class TrayUnreadTest {
    @Test
    fun formatUnreadTrayLabelMatchesDesktopContract() {
        assertEquals("AetherFeed — 0 unread", formatUnreadTrayLabel(0))
        assertEquals("AetherFeed — 12 unread", formatUnreadTrayLabel(12))
    }

    @Test
    fun totalUnreadCountsNonReadStates() {
        val states = listOf(ReadStatus.Unread, ReadStatus.InProgress, ReadStatus.Read, ReadStatus.Unread)
        assertEquals(3, totalUnread(states))
    }
}
package org.aetherfeed.shared.domain.unread

import org.aetherfeed.shared.domain.ReadStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class WidgetUnreadSnapshotTest {
    @Test
    fun roundTripJson() {
        val snap = WidgetUnreadSnapshot(unreadTotal = 7, updatedAtEpochMs = 1_700_000_000_000)
        val json = encodeWidgetUnreadSnapshot(snap)
        assertEquals(snap, decodeWidgetUnreadSnapshot(json))
        assertEquals("Unread: 7", snap.widgetLine())
    }

    @Test
    fun totalUnreadMatchesTrayContract() {
        assertEquals(2, totalUnread(listOf(ReadStatus.Unread, ReadStatus.InProgress, ReadStatus.Read)))
    }
}
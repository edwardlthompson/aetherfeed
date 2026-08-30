package org.aetherfeed.app.notify

import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class UnreadChromeTest {
    @Test
    fun countsNonReadStates() {
        val states = listOf(
            ReadState("a", ModuleKind.News, ReadStatus.Unread, 1),
            ReadState("b", ModuleKind.News, ReadStatus.Read, 1),
        )
        assertEquals(1, unreadTotal(states))
    }
}

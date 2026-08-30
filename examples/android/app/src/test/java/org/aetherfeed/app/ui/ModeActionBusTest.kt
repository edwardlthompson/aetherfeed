package org.aetherfeed.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModeActionBusTest {
    @Test
    fun publishBumpsRevisionAndClearDropsNews() {
        val bus = ModeActionBus()
        var opened = 0
        bus.next = { opened += 1 }
        bus.publish()
        val first = bus.revision
        bus.next?.invoke()
        bus.clearNews()
        assertEquals(1, opened)
        assertNull(bus.next)
        assertEquals(first + 1, bus.revision)
    }
}

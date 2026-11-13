package org.aetherfeed.app.notify

import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.data.InMemoryLibrary
import org.aetherfeed.app.data.defaultUnread
import org.junit.Assert.assertEquals
import org.junit.Test

class UnreadTotalTest {
    @Test
    fun mirrorsLibraryUnread() =
        runBlocking {
            val library = InMemoryLibrary()
            library.upsertReadState(defaultUnread("a1", 1L))
            val unread = LibraryUnreadTotal(library)
            assertEquals(1, unread.current())
            assertEquals(0, unread.channels().size)
        }
}

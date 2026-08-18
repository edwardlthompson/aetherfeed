package org.aetherfeed.app.domain

import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.data.InMemoryLibrary
import org.aetherfeed.app.data.defaultUnread
import org.junit.Assert.assertEquals
import org.junit.Test

class UnreadCountTest {
    @Test
    fun countsOnlyNonReadItems() = runBlocking {
        val library = InMemoryLibrary()
        library.upsertReadState(defaultUnread("a", 1))
        library.upsertReadState(
            ReadState("b", ModuleKind.Podcast, ReadStatus.InProgress, 2),
        )
        library.upsertReadState(
            ReadState("c", ModuleKind.Booru, ReadStatus.Read, 3),
        )
        assertEquals(2, library.unreadCount())
    }
}

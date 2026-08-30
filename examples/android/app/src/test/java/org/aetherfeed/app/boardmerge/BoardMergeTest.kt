package org.aetherfeed.app.boardmerge

import org.aetherfeed.app.domain.BooruPost
import org.junit.Assert.assertEquals
import org.junit.Test

class BoardMergeTest {
    @Test
    fun mergesSameRemoteId() {
        val a = post("1", "dan", "99")
        val b = post("2", "gel", "99")
        val merged = mergePosts(listOf(a, b))
        assertEquals(1, merged.size)
        assertEquals(setOf("dan", "gel"), merged[0].sources)
        assertEquals("note", withNote(merged[0], "note").note)
    }

    private fun post(id: String, source: String, remote: String) = BooruPost(
        id = id,
        sourceId = source,
        remoteId = remote,
        fileUrl = "https://example.invalid/$id",
        tags = emptyList(),
    )
}

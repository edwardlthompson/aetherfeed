package org.aetherfeed.app.booru

import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.domain.BooruPost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BooruSessionTest {
    @Test
    fun parseQuerySplitsTags() {
        val session = BooruSession(LocalBooruClient())
        assertEquals(listOf("sky", "tree"), session.parseQuery("  sky   tree ").tags)
        assertTrue(session.parseQuery("").tags.isEmpty())
    }

    @Test
    fun searchUsesFixturesAndKeepsDesignedEmpty() = runTest {
        val post = samplePost("p1", listOf("landscape"))
        val session = BooruSession(LocalBooruClient(fixtures = listOf(post)))
        val empty = session.search("")
        assertTrue(empty.designedEmpty)
        assertFalse(empty.failed)
        val hits = session.search("landscape")
        assertEquals(listOf("p1"), hits.posts.map { it.id })
        assertFalse(hits.designedEmpty)
    }

    @Test
    fun searchFailureYieldsDesignedEmpty() = runTest {
        val session = BooruSession(ThrowingBooruClient())
        val snap = session.search("landscape")
        assertTrue(snap.failed)
        assertTrue(snap.designedEmpty)
        assertTrue(snap.posts.isEmpty())
    }

    @Test
    fun toggleFavoriteTracksLocalList() = runTest {
        val post = samplePost("p1", listOf("landscape"))
        val session = BooruSession(LocalBooruClient(fixtures = listOf(post)))
        session.search("landscape")
        assertTrue(session.toggleFavorite(post))
        assertTrue(session.isFavorite("p1"))
        assertEquals(listOf("p1"), session.favorites().map { it.id })
        assertFalse(session.toggleFavorite(post))
        assertTrue(session.favorites().isEmpty())
    }

    private fun samplePost(id: String, tags: List<String>): BooruPost = BooruPost(
        id = id,
        sourceId = "local",
        remoteId = id,
        fileUrl = "https://example.invalid/$id.jpg",
        tags = tags,
    )

    private class ThrowingBooruClient : BooruClient {
        override suspend fun search(source: BooruSource, query: TagQuery): List<BooruPost> {
            error("offline")
        }

        override fun isBlacklisted(tag: String): Boolean = false
    }
}

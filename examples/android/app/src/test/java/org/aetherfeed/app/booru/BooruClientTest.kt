package org.aetherfeed.app.booru

import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.domain.BooruPost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BooruClientTest {
    @Test
    fun localClientHonorsBlacklist() {
        val client = LocalBooruClient(blacklist = setOf("spam"))
        assertTrue(client.isBlacklisted("SPAM"))
        assertFalse(client.isBlacklisted("safe"))
    }

    @Test
    fun productionSearchStaysEmptyWithoutCrash() = runTest {
        val client = LocalBooruClient()
        val source = BooruSession.localBoardsSource()
        assertTrue(client.search(source, TagQuery(tags = emptyList())).isEmpty())
        assertTrue(client.search(source, TagQuery(tags = listOf("  ", ""))).isEmpty())
        assertTrue(client.search(source, TagQuery(tags = listOf("landscape"))).isEmpty())
    }

    @Test
    fun fixturesFilterByAllQueryTags() = runTest {
        val client = LocalBooruClient(
            fixtures = listOf(
                samplePost("p1", listOf("landscape", "sky")),
                samplePost("p2", listOf("portrait")),
            ),
        )
        val hits = client.search(
            BooruSession.localBoardsSource(),
            TagQuery(tags = listOf("Landscape")),
        )
        assertEquals(listOf("p1"), hits.map { it.id })
    }

    @Test
    fun fixturesDropBlacklistedPostsAndQueries() = runTest {
        val client = LocalBooruClient(
            blacklist = setOf("spam"),
            fixtures = listOf(
                samplePost("ok", listOf("landscape")),
                samplePost("bad", listOf("landscape", "spam")),
            ),
        )
        val source = BooruSession.localBoardsSource()
        assertEquals(listOf("ok"), client.search(source, TagQuery(tags = listOf("landscape"))).map { it.id })
        assertTrue(client.search(source, TagQuery(tags = listOf("spam"))).isEmpty())
    }

    private fun samplePost(id: String, tags: List<String>): BooruPost = BooruPost(
        id = id,
        sourceId = "local",
        remoteId = id,
        fileUrl = "https://example.invalid/$id.jpg",
        tags = tags,
    )
}

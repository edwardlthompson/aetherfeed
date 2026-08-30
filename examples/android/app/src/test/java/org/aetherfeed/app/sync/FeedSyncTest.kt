package org.aetherfeed.app.sync

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class FeedSyncTest {
    private fun feed(url: String, updatedAt: Long, title: String = "A") = Feed(
        id = "x",
        title = title,
        url = url,
        kind = ModuleKind.News,
        updatedAt = updatedAt,
    )

    @Test
    fun mergeKeepsNewerNormalizedUrl() {
        val merged = mergeFeedSources(
            listOf(feed("HTTP://WWW.Example.invalid/rss.xml/", 1, "Old")),
            listOf(feed("https://example.invalid/rss.xml", 9, "New")),
        )
        assertEquals(1, merged.size)
        assertEquals("New", merged[0].title)
        assertEquals("feed:https://example.invalid/rss.xml", merged[0].id)
    }

    @Test
    fun documentRoundTrip() {
        val encoded = encodeFeedDocument(listOf(feed("https://example.invalid/a.xml", 2)), 10)
        val decoded = decodeFeedDocument(encoded)
        assertEquals("https://example.invalid/a.xml", decoded[0].url)
    }
}

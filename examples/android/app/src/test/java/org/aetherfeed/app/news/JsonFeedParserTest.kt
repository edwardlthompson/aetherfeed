package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class JsonFeedParserTest {
    @Test
    fun sniffsAndParsesJsonFeed() {
        val body = """
            {"version":"https://jsonfeed.org/version/1.1","title":"Local",
             "items":[{"id":"1","title":"Hello","url":"https://example.invalid/1"}]}
        """.trimIndent()
        assertEquals(FeedFormat.JsonFeed, sniffFeedFormat(body))
        val feed = parseJsonFeed(body)
        assertEquals("Local", feed.title)
        assertEquals("Hello", feed.items.single().title)
    }
}

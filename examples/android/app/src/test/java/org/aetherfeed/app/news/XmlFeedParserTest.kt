package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XmlFeedParserTest {
    @Test
    fun parsesRssItems() {
        val body = """
            <rss><channel>
              <title>Desk</title>
              <link>https://example.invalid</link>
              <item><title>One</title><link>https://example.invalid/1</link><guid>ONE</guid></item>
            </channel></rss>
        """.trimIndent()
        assertEquals(FeedFormat.Rss, sniffFeedFormat(body))
        val feed = parseRssFeed(body)
        assertEquals("Desk", feed.title)
        assertEquals("ONE", feed.items.single().id)
        assertEquals("One", feed.items.single().title)
    }

    @Test
    fun rssDescriptionIsNotFullContent() {
        val body = """
            <rss><channel>
              <item>
                <title>Moto</title>
                <link>https://motoiq.example/a</link>
                <description><![CDATA[<img class="webfeedsFeaturedVisual" src="https://cdn.example/a.jpg" link_thumbnail="1"><p>${"Word ".repeat(80)}</p>]]></description>
              </item>
            </channel></rss>
        """.trimIndent()
        val item = parseRssFeed(body).items.single()
        assertEquals(null, item.contentHtml)
        assertTrue(item.summary.orEmpty().contains("Word"))
    }

    @Test
    fun parsesAtomEntries() {
        val body = """
            <feed xmlns="http://www.w3.org/2005/Atom">
              <title>Desk</title>
              <link href="https://example.invalid"/>
              <entry><id>e1</id><title>Two</title><link href="https://example.invalid/2"/></entry>
            </feed>
        """.trimIndent()
        assertEquals(FeedFormat.Atom, sniffFeedFormat(body))
        val feed = parseAtomFeed(body)
        assertEquals("Desk", feed.title)
        assertEquals("e1", feed.items.single().id)
    }
}

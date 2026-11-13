package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class FeedParsersTest {
    @Test
    fun parsesJsonFeed() {
        val json =
            """
            {"title":"Demo","feed_url":"https://ex.test/feed.json","items":[
              {"id":"1","url":"https://ex.test/1","title":"Hello","summary":"Hi"}
            ]}
            """.trimIndent()
        val doc = parseFeed(json.toByteArray(), "application/feed+json", "https://ex.test/feed.json", 10L)
        assertEquals(1, doc.feeds.size)
        assertEquals("Demo", doc.feeds[0].title)
        assertEquals(1, doc.articles.size)
        assertEquals("Hello", doc.articles[0].title)
    }

    @Test
    fun parsesRssFeed() {
        val rss =
            """
            <?xml version="1.0"?>
            <rss version="2.0">
              <channel>
                <title>Weekly</title>
                <link>https://ex.test/</link>
                <item>
                  <title>Alpha</title>
                  <link>https://ex.test/a</link>
                  <guid>guid-a</guid>
                  <description>Body A</description>
                </item>
              </channel>
            </rss>
            """.trimIndent()
        val doc = parseFeed(rss.toByteArray(), "application/rss+xml", "https://ex.test/rss", 20L)
        assertEquals("Weekly", doc.feeds.single().title)
        assertEquals(1, doc.articles.size)
        assertEquals("Alpha", doc.articles[0].title)
        assertEquals("guid-a", doc.articles[0].id)
        assertEquals("Body A", doc.articles[0].summary)
    }

    @Test
    fun parsesAtomFeed() {
        val atom =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
              <title>Atom Demo</title>
              <link href="https://ex.test/" rel="alternate"/>
              <id>https://ex.test/feed</id>
              <entry>
                <id>urn:uuid:entry-1</id>
                <title>Beta</title>
                <link href="https://ex.test/b" rel="alternate"/>
                <summary>Snippet B</summary>
              </entry>
            </feed>
            """.trimIndent()
        val doc = parseFeed(atom.toByteArray(), "application/atom+xml", "https://ex.test/atom", 30L)
        assertEquals("Atom Demo", doc.feeds.single().title)
        assertEquals(1, doc.articles.size)
        assertEquals("Beta", doc.articles[0].title)
        assertEquals("urn:uuid:entry-1", doc.articles[0].id)
        assertEquals("https://ex.test/b", doc.articles[0].url)
        assertEquals("Snippet B", doc.articles[0].summary)
    }

    @Test
    fun roundTripsOpml() {
        val opml =
            """
            <?xml version="1.0"?>
            <opml version="2.0"><body>
            <outline type="rss" text="A" title="A" xmlUrl="https://a.test/rss" htmlUrl="https://a.test"/>
            </body></opml>
            """.trimIndent()
        val feeds = parseOpml(opml.toByteArray(), 5L)
        assertEquals(1, feeds.size)
        val exported = exportOpml(feeds).toString(Charsets.UTF_8)
        assertTrue(exported.contains("xmlUrl=\"https://a.test/rss\""))
    }
}

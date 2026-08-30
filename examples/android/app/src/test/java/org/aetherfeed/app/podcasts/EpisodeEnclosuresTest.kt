package org.aetherfeed.app.podcasts

import org.junit.Assert.assertEquals
import org.junit.Test

class EpisodeEnclosuresTest {
    @Test
    fun readsEnclosureUrl() {
        val xml = """<rss><channel><item><enclosure url="https://cdn.example/ep.mp3" /></item></channel></rss>"""
        assertEquals("https://cdn.example/ep.mp3", enclosureFromRss(xml))
    }
}

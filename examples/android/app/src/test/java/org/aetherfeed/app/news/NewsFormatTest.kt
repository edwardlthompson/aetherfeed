package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsFormatTest {
    @Test
    fun stripsHtmlAndCapsSnippet() {
        val long = "<p>" + "word ".repeat(80) + "</p>"
        val snippet = plainSnippet(long, 40)
        assertTrue(snippet.startsWith("word"))
        assertTrue(snippet.endsWith("…"))
        assertTrue("<" !in snippet)
    }

    @Test
    fun listSnippetDropsLinkStubs() {
        assertEquals("", listSnippet("<p>Article URL: https://ex.example/a</p>"))
        assertEquals("Hello world", listSnippet("<p>Hello world</p>"))
    }

    @Test
    fun agesMatchReadYouStyleBuckets() {
        val now = 100_000_000L
        assertEquals(AgeUnit.Empty, ageLabel(null, now).unit)
        assertEquals(AgeUnit.Now, ageLabel(now - 10 * 60_000L, now).unit)
        assertEquals(AgeLabel(AgeUnit.Hours, 5), ageLabel(now - 5 * 3_600_000L, now))
        assertEquals(AgeLabel(AgeUnit.Days, 3), ageLabel(now - 3 * 86_400_000L, now))
    }
}

package org.aetherfeed.app.readerimport

import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.data.InMemoryLibrary
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderImportTest {
    private val opml =
        """<opml><body><outline title="Local" xmlUrl="https://example.invalid/rss.xml" htmlUrl="https://example.invalid"/></body></opml>"""

    @Test
    fun detectsTakeoutAndParsesSubscriptions() {
        val file = ReaderImportFile(
            name = "takeout.zip",
            members = mapOf("Takeout/Reader/subscriptions.xml" to opml),
        )
        assertEquals(ReaderVendor.GoogleReader, detectReaderVendor(file))
        val parsed = parseReaderImport(file)
        assertEquals(1, parsed.outlines.size)
        assertEquals("https://example.invalid/rss.xml", parsed.outlines[0].xmlUrl)
    }

    @Test
    fun detectsInoreaderAndGenericOpml() {
        assertEquals(
            ReaderVendor.Inoreader,
            detectReaderVendor(ReaderImportFile("export.opml", "<opml><head><title>Inoreader</title></head>$opml")),
        )
        val parsed = parseReaderImport(ReaderImportFile("feedly.opml", opml))
        assertEquals(ReaderVendor.Opml, parsed.vendor)
        assertEquals(1, parsed.outlines.size)
    }

    @Test
    fun rejectsEmptyAndMalformedWithoutFeeds() {
        val empty = parseReaderImport(ReaderImportFile("empty.opml", "  "))
        assertEquals(ReaderImportErrorCode.Empty, empty.errors[0].code)
        val bad = parseReaderImport(ReaderImportFile("backup.json", "{not-json"))
        assertTrue(bad.errors.any { it.code == ReaderImportErrorCode.Malformed })
        assertTrue(bad.outlines.isEmpty())
    }

    @Test
    fun applySkipsDuplicatesAndLeavesVaultOnHardFailure() = runBlocking {
        val library = InMemoryLibrary()
        library.upsertFeed(
            Feed("existing", "Old", "https://example.invalid/rss.xml", ModuleKind.News, updatedAt = 1),
        )
        val skip = ReaderImportRepository(library).apply(parseReaderImport(ReaderImportFile("feedly.opml", opml)), 10)
        assertEquals(0, skip.feedsAdded)
        assertEquals(1, skip.feedsSkipped)
        val failed = ReaderImportRepository(InMemoryLibrary()).apply(
            parseReaderImport(ReaderImportFile("bad.json", "{nope")),
            10,
        )
        assertEquals(0, failed.feedsAdded)
        assertTrue(failed.errors.any { it.code == ReaderImportErrorCode.Malformed })
    }

    @Test
    fun normalizesDuplicatesAndSortsPodcasts() = runBlocking {
        val library = InMemoryLibrary()
        val mixed = """
            <opml><body>
            <outline text="Podcasts"><outline title="Cast" xmlUrl="https://feeds.libsyn.com/9/rss"/></outline>
            <outline title="News" xmlUrl="HTTP://WWW.example.invalid/rss.xml/"/>
            <outline title="Dup" xmlUrl="https://example.invalid/rss.xml"/>
            </body></opml>
        """.trimIndent()
        val result = ReaderImportRepository(library).apply(parseReaderImport(ReaderImportFile("greader.opml", mixed)), 10)
        assertEquals(2, result.feedsAdded)
        assertEquals(1, result.feedsSkipped)
        assertEquals(1, result.newsAdded)
        assertEquals(1, result.podcastsAdded)
        assertEquals("https://example.invalid/rss.xml", normalizeFeedUrl("HTTP://WWW.example.invalid/rss.xml/"))
        val zip = bytesToImportFile("empty.zip", ByteArray(0))
        assertEquals("", zip.text)
    }

    @Test
    fun parsesInoreaderJsonAndTakeoutStars() {
        val ino = parseReaderImport(
            ReaderImportFile(
                "inoreader-backup.json",
                """{"feeds":[{"title":"Local","feedUrl":"https://example.invalid/rss.xml"}]}""",
            ),
        )
        assertEquals(ReaderVendor.Inoreader, ino.vendor)
        assertEquals("https://example.invalid/rss.xml", ino.outlines[0].xmlUrl)
        val members = mapOf(
            "Takeout/Reader/subscriptions.xml" to opml,
            "Takeout/Reader/starred.json" to
                """{"title":"Starred items","items":[{"title":"Saved","canonical":[{"href":"https://example.invalid/s"}]}]}""",
        )
        assertEquals(opml, takeoutSubscriptionsXml(members))
        val takeout = parseReaderImport(ReaderImportFile("takeout.zip", members = members))
        assertEquals(1, takeout.stars.size)
        assertTrue(readerImportSummary(ReaderImportResult(ReaderVendor.GoogleReader, 1, 0, 1, emptyList())).contains("stars=1"))
    }

    @Test
    fun parsesAetherFeedLibraryKindAndFolder() = runBlocking {
        val parsed = parseReaderImport(
            ReaderImportFile(
                "seed-library.json",
                """[{"title":"Local","url":"https://example.invalid/rss.xml","kind":"news","folder":"Art"}]""",
            ),
        )
        assertEquals("Art", parsed.outlines.single().folder)
        assertEquals("news", parsed.outlines.single().type)
        val planned = planReaderImport(emptySet(), parsed, 1)
        assertEquals("Art", planned.feeds.single().folder)
        assertEquals(ModuleKind.News, planned.feeds.single().kind)
    }
}

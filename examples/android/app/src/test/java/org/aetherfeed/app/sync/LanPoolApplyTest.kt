package org.aetherfeed.app.sync

import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.data.InMemoryLibrary
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanPoolApplyTest {
    @Test
    fun applyDocumentWritesSubsReadsStarsPlayback() =
        runBlocking {
            val library = InMemoryLibrary()
            val doc =
                PoolDocument(
                    version = 2,
                    updatedAt = 9,
                    subs =
                        listOf(
                            PoolSub(url = "https://news.example/rss", title = "N", kind = "news", updatedAt = 1),
                        ),
                    reads = listOf(PoolRead(url = "https://news.example/a", read = true, updatedAt = 2)),
                    stars = listOf(PoolStar(url = "https://news.example/a", starred = true, updatedAt = 3)),
                    playback = listOf(PoolPlayback(url = "https://pod.example/ep.mp3", positionMs = 42, updatedAt = 4)),
                )
            LanPoolApply.applyDocument(library, doc, null)
            assertEquals(1, library.feeds().size)
            assertEquals(ReadStatus.Read, library.readState("https://news.example/a")?.status)
            assertEquals(1, library.stars().size)
            assertEquals(42L, library.playback("https://pod.example/ep.mp3")?.positionMs)
            val snap = LanPoolApply.snapshotLocal(library)
            assertTrue(snap.subs.any { it.url.contains("news.example") })
            assertEquals(1, snap.reads.size)
        }

    @Test
    fun snapshotIncludesLocalFeeds() =
        runBlocking {
            val library = InMemoryLibrary()
            library.upsertFeed(
                Feed(id = "1", title = "T", url = "https://x.example/f", kind = ModuleKind.Podcast, updatedAt = 5),
            )
            val snap = LanPoolApply.snapshotLocal(library)
            assertEquals("podcast", snap.subs.single().kind)
        }

    @Test
    fun applyReadMapsPermalinkOntoArticleGuid() =
        runBlocking {
            val library = InMemoryLibrary()
            library.upsertArticle(
                Article(
                    id = "guid-42",
                    feedId = "f",
                    title = "T",
                    url = "https://news.example/a",
                ),
            )
            LanPoolApply.applyDocument(
                library,
                PoolDocument(
                    version = 2,
                    reads = listOf(PoolRead(url = "https://news.example/a", read = true, updatedAt = 9)),
                    stars = listOf(PoolStar(url = "https://news.example/a", starred = true, updatedAt = 9)),
                ),
                null,
            )
            assertEquals(ReadStatus.Read, library.readState("guid-42")?.status)
            assertEquals("guid-42", library.stars().single().targetId)
            val snap = LanPoolApply.snapshotLocal(library)
            assertEquals("https://news.example/a", snap.reads.single().url)
            assertEquals("https://news.example/a", snap.stars.single().url)
        }
}

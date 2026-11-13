package org.aetherfeed.app.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26], application = android.app.Application::class)
class SubscriptionPoolCodecTest {
    @Test
    fun roundTripPreservesFields() {
        val doc =
            PoolDocument(
                version = 2,
                updatedAt = 42L,
                subs =
                    listOf(
                        PoolSub(
                            url = "https://example.test/rss/",
                            title = "Example",
                            kind = "news",
                            tag = "tech",
                            updatedAt = 9L,
                        ),
                        PoolSub(
                            url = "https://pods.example/feed",
                            title = "Pods",
                            kind = "podcast",
                            updatedAt = 3L,
                        ),
                    ),
                reads = listOf(PoolRead(url = "https://a.example/1", read = true, updatedAt = 2L)),
                stars = listOf(PoolStar(url = "https://a.example/1", starred = true, updatedAt = 3L)),
                playback =
                    listOf(PoolPlayback(url = "https://pods.example/ep.mp3", positionMs = 1500L, updatedAt = 4L)),
            )
        val decoded = SubscriptionPoolCodec.decode(SubscriptionPoolCodec.encode(doc))
        assertEquals(2, decoded.version)
        assertEquals(42L, decoded.updatedAt)
        assertEquals("https://example.test/rss", decoded.subs[0].url)
        assertEquals("news", decoded.subs[0].kind)
        assertEquals("podcast", decoded.subs[1].kind)
        assertEquals(1, decoded.reads.size)
        assertEquals(1, decoded.stars.size)
        assertEquals(1500L, decoded.playback.single().positionMs)
    }

    @Test
    fun upgradesV1Document() {
        val raw =
            """{"version":1,"updatedAt":1,"subs":[{"url":"https://b.example","title":"x","kind":"booru","tag":"","updatedAt":1}]}"""
        val doc = SubscriptionPoolCodec.decode(raw)
        assertEquals(2, doc.version)
        assertEquals("booru", doc.subs.single().kind)
        assertTrue(doc.reads.isEmpty())
        assertTrue(doc.stars.isEmpty())
        assertTrue(doc.playback.isEmpty())
        assertTrue(SubscriptionPoolCodec.isLauncherKind("news"))
        assertTrue(!SubscriptionPoolCodec.isLauncherKind("booru"))
    }

    @Test
    fun rejectUnknownKind() {
        val raw =
            """{"version":2,"updatedAt":1,"subs":[{"url":"https://x.example","title":"x","kind":"video","tag":"","updatedAt":1}]}"""
        assertThrows(PoolCodecError.InvalidKind::class.java) {
            SubscriptionPoolCodec.decode(raw)
        }
    }

    @Test
    fun lwwHigherWinsEqualKeepsLocal() {
        val local =
            listOf(
                PoolSub(url = "https://a.example/f", title = "Local", kind = "news", updatedAt = 5),
                PoolSub(url = "https://b.example/f", title = "Keep", kind = "news", updatedAt = 10),
            )
        val incoming =
            listOf(
                PoolSub(url = "https://a.example/f/", title = "Remote", kind = "news", updatedAt = 7),
                PoolSub(url = "https://b.example/f", title = "Lose", kind = "news", updatedAt = 10),
            )
        val merged = SubscriptionPoolCodec.mergeLww(local, incoming)
        assertEquals("Remote", merged.first { it.url == "https://a.example/f" }.title)
        assertEquals("Keep", merged.first { it.url == "https://b.example/f" }.title)
    }

    @Test
    fun mergeStarsAndPlaybackTies() {
        val stars =
            PoolMerge.mergeStars(
                listOf(PoolStar(url = "https://a.example/1", starred = false, updatedAt = 5)),
                listOf(PoolStar(url = "https://a.example/1", starred = true, updatedAt = 5)),
            )
        assertTrue(stars.single().starred)
        val playback =
            PoolMerge.mergePlayback(
                listOf(PoolPlayback(url = "https://ep.mp3", positionMs = 10, updatedAt = 3)),
                listOf(PoolPlayback(url = "https://ep.mp3", positionMs = 99, updatedAt = 3)),
            )
        assertEquals(99L, playback.single().positionMs)
    }

    @Test
    fun blankUrlRejected() {
        assertThrows(PoolCodecError.BlankUrl::class.java) {
            SubscriptionPoolCodec.requireValid(
                PoolDocument(subs = listOf(PoolSub(url = "  ", kind = "news"))),
            )
        }
    }

    @Test
    fun emptySubsAllowed() {
        val raw = SubscriptionPoolCodec.encode(PoolDocument(updatedAt = 1L, subs = emptyList()))
        assertTrue(SubscriptionPoolCodec.decode(raw).subs.isEmpty())
    }
}

package org.aetherfeed.app.podcasts

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PodcastPlayerTest {
    @Test
    fun noopLeavesPositionEmpty() = runTest {
        val player = NoopPodcastPlayer()
        player.play("ep-1")
        assertNull(player.position("ep-1"))
    }

    @Test
    fun playPersistsZeroPosition() = runTest {
        val player = InProcessPodcastPlayer { 1_000L }
        player.play("ep-1")
        val pos = player.position("ep-1")
        assertEquals("ep-1", pos?.episodeId)
        assertEquals(0L, pos?.positionMs)
    }

    @Test
    fun positionAdvancesWhilePlayingThenFreezesOnPause() = runTest {
        var now = 1_000L
        val player = InProcessPodcastPlayer { now }
        player.play("ep-1")
        now = 2_500L
        assertEquals(1_500L, player.position("ep-1")?.positionMs)
        player.pause()
        now = 9_000L
        assertEquals(1_500L, player.position("ep-1")?.positionMs)
    }

    @Test
    fun seekToUpdatesPersistedPosition() = runTest {
        val player = InProcessPodcastPlayer { 1_000L }
        player.play("ep-1")
        player.seekTo(8_000L)
        assertEquals(8_000L, player.position("ep-1")?.positionMs)
        player.seekTo(-40L)
        assertEquals(0L, player.position("ep-1")?.positionMs)
    }

    @Test
    fun playResumesStoredPosition() = runTest {
        var now = 1_000L
        val player = InProcessPodcastPlayer { now }
        player.play("ep-1")
        now = 4_000L
        player.pause()
        player.play("ep-1")
        assertEquals(3_000L, player.position("ep-1")?.positionMs)
    }

    @Test
    fun otherEpisodeKeepsFirstPosition() = runTest {
        var now = 1_000L
        val player = InProcessPodcastPlayer { now }
        player.play("ep-1")
        now = 2_000L
        player.play("ep-2")
        assertEquals(1_000L, player.position("ep-1")?.positionMs)
        assertEquals(0L, player.position("ep-2")?.positionMs)
    }

    @Test
    fun unknownEpisodeAndSeekWithoutPlayAreNoops() = runTest {
        val player = InProcessPodcastPlayer()
        assertNull(player.position("missing"))
        player.seekTo(500L)
        player.pause()
        assertNull(player.position("missing"))
    }

    @Test
    fun playRejectsBlankId() = runTest {
        val player = InProcessPodcastPlayer()
        val result = runCatching { player.play("  ") }
        assertTrue(result.isFailure)
        assertNull(player.position("  "))
    }
}

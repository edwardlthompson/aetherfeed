package org.aetherfeed.app.podcasts

import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.data.InMemoryLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PodcastPlayerTest {
  private val episodeId = "ep-1"
  private val library = InMemoryLibrary()
  private val repository = LibraryPodcastRepository(library)

  @Test
  fun playPauseAndPersistPosition() =
      runBlocking {
        val surface = FakePlaybackSurface(positionMs = 42_000L, durationMs = 90_000L)
        val player = PodcastPlayer(episodeId, surface, repository, now = { 100L })

        player.apply(PlaybackCommand.Play)
        assertTrue(surface.playing)

        player.apply(PlaybackCommand.Pause)
        assertFalse(surface.playing)
        val saved = library.playback(episodeId)
        assertEquals(42_000L, saved?.positionMs)
        assertEquals(90_000L, saved?.durationMs)
        assertEquals(100L, saved?.updatedAt)
      }

  @Test
  fun seekSkipAndSetSpeed() =
      runBlocking {
        val surface = FakePlaybackSurface(positionMs = 10_000L, durationMs = 120_000L)
        val player =
            PodcastPlayer(episodeId, surface, repository, now = { 1L }, skipStepMs = 5_000L)

        player.apply(PlaybackCommand.Seek(25_000L))
        assertEquals(25_000L, surface.positionMs)
        assertEquals(25_000L, library.playback(episodeId)?.positionMs)

        player.apply(PlaybackCommand.Skip(2))
        assertEquals(35_000L, surface.positionMs)

        player.apply(PlaybackCommand.SetSpeed(1.5f))
        assertEquals(1.5f, surface.playbackSpeed, 0.001f)
      }

  private class FakePlaybackSurface(
      var positionMs: Long,
      private val durationMs: Long,
  ) : PodcastPlaybackSurface {
    var playing = false
    var playbackSpeed = 1f

    override fun play() {
      playing = true
    }

    override fun pause() {
      playing = false
    }

    override fun seekTo(positionMs: Long) {
      this.positionMs = positionMs
    }

    override fun setSpeed(rate: Float) {
      playbackSpeed = rate
    }

    override fun currentPositionMs(): Long = positionMs

    override fun durationMs(): Long? = durationMs
  }
}

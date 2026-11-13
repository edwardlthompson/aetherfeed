package org.aetherfeed.app.podcasts

import androidx.media3.common.Player
import org.aetherfeed.app.domain.PlaybackPosition

/** Testable playback surface; wraps Media3 [Player] in production. */
interface PodcastPlaybackSurface {
    fun play()

    fun pause()

    fun seekTo(positionMs: Long)

    fun setSpeed(rate: Float)

    fun currentPositionMs(): Long

    fun durationMs(): Long?
}

class Media3PlaybackSurface(private val player: Player) : PodcastPlaybackSurface {
    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    override fun setSpeed(rate: Float) {
        player.setPlaybackSpeed(rate)
    }

    override fun currentPositionMs(): Long = player.currentPosition

    override fun durationMs(): Long? =
        player.duration.takeIf { it > 0 && it != androidx.media3.common.C.TIME_UNSET }
}

class PodcastPlayer(
    private val episodeId: String,
    private val playback: PodcastPlaybackSurface,
    private val repository: PodcastRepository,
    private val now: () -> Long = { System.currentTimeMillis() },
    private val skipStepMs: Long = 30_000L,
) {
    suspend fun apply(command: PlaybackCommand) {
        when (command) {
            PlaybackCommand.Play -> playback.play()
            PlaybackCommand.Pause -> {
                playback.pause()
                persistPosition()
            }
            is PlaybackCommand.Seek -> {
                playback.seekTo(command.positionMs)
                persistPosition()
            }
            is PlaybackCommand.Skip -> {
                val target = (playback.currentPositionMs() + command.delta * skipStepMs).coerceAtLeast(0L)
                playback.seekTo(target)
                persistPosition()
            }
            is PlaybackCommand.SetSpeed -> playback.setSpeed(command.rate)
        }
    }

    private suspend fun persistPosition() {
        repository.savePosition(
            PlaybackPosition(
                episodeId = episodeId,
                positionMs = playback.currentPositionMs(),
                durationMs = playback.durationMs(),
                updatedAt = now(),
            ),
        )
    }
}

fun PodcastPlayer(
    episodeId: String,
    player: Player,
    repository: PodcastRepository,
    now: () -> Long = { System.currentTimeMillis() },
): PodcastPlayer =
    PodcastPlayer(episodeId, Media3PlaybackSurface(player), repository, now)

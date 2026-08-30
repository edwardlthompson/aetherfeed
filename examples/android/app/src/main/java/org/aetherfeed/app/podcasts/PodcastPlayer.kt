package org.aetherfeed.app.podcasts

import org.aetherfeed.app.domain.PlaybackPosition

/** Playback port. Production chrome uses [Media3PodcastPlayer]; tests use [InProcessPodcastPlayer]. */
interface PodcastPlayer {
    suspend fun play(episodeId: String)
    suspend fun pause()
    suspend fun seekTo(positionMs: Long)
    fun position(episodeId: String): PlaybackPosition?
}

/** Hermetic test double; never records position. */
class NoopPodcastPlayer : PodcastPlayer {
    override suspend fun play(episodeId: String) = Unit
    override suspend fun pause() = Unit
    override suspend fun seekTo(positionMs: Long) = Unit
    override fun position(episodeId: String): PlaybackPosition? = null
}

/** In-process player that persists per-episode position. Not a Media3 session. */
class InProcessPodcastPlayer(
    private val clockMs: () -> Long = { System.currentTimeMillis() },
) : PodcastPlayer {
    private val stored = mutableMapOf<String, PlaybackPosition>()
    private var currentId: String? = null
    private var playing = false
    private var markedAt = 0L

    override suspend fun play(episodeId: String) {
        require(episodeId.isNotBlank()) { "episodeId" }
        commitElapsed()
        currentId = episodeId
        stored.putIfAbsent(episodeId, PlaybackPosition(episodeId, 0L, null, clockMs()))
        playing = true
        markedAt = clockMs()
    }

    override suspend fun pause() {
        commitElapsed()
        playing = false
    }

    override suspend fun seekTo(positionMs: Long) {
        val id = currentId ?: return
        commitElapsed()
        val prior = stored[id]
        stored[id] = PlaybackPosition(id, positionMs.coerceAtLeast(0L), prior?.durationMs, clockMs())
        markedAt = clockMs()
    }

    override fun position(episodeId: String): PlaybackPosition? {
        if (currentId == episodeId) commitElapsed()
        return stored[episodeId]
    }

    private fun commitElapsed() {
        val id = currentId ?: return
        if (!playing) return
        val now = clockMs()
        val prior = stored[id] ?: PlaybackPosition(id, 0L, null, now)
        stored[id] = prior.copy(positionMs = prior.positionMs + (now - markedAt).coerceAtLeast(0L), updatedAt = now)
        markedAt = now
    }
}

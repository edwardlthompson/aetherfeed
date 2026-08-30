package org.aetherfeed.app.podcasts

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import org.aetherfeed.app.domain.PlaybackPosition

class Media3PodcastPlayer(
    context: Context,
    private val resolveUrl: (String) -> String? = { null },
) : PodcastPlayer {
    private val exo = ExoPlayer.Builder(context.applicationContext).build()
    private var currentId: String? = null

    override suspend fun play(episodeId: String) {
        val url = resolveUrl(episodeId) ?: return
        if (currentId != episodeId) {
            exo.setMediaItem(MediaItem.fromUri(url))
            exo.prepare()
            currentId = episodeId
        }
        exo.play()
    }

    override suspend fun pause() {
        exo.pause()
    }

    override suspend fun seekTo(positionMs: Long) {
        exo.seekTo(positionMs.coerceAtLeast(0L))
    }

    override fun position(episodeId: String): PlaybackPosition =
        PlaybackPosition(episodeId, exo.currentPosition, exo.duration.takeIf { it > 0 }, System.currentTimeMillis())
}

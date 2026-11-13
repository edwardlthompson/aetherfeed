package org.aetherfeed.app.podcasts

import org.aetherfeed.app.domain.Episode
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.PlaybackPosition
import org.aetherfeed.app.domain.PodcastShow

data class PlaybackQueue(
    val episodeIds: List<String>,
    val currentIndex: Int = 0,
)

sealed class PlaybackCommand {
    data object Play : PlaybackCommand()

    data object Pause : PlaybackCommand()

    data class Seek(val positionMs: Long) : PlaybackCommand()

    data class Skip(val delta: Int) : PlaybackCommand()

    data class SetSpeed(val rate: Float) : PlaybackCommand()
}

sealed class EpisodeDownloadState {
    data object None : EpisodeDownloadState()

    data object Queued : EpisodeDownloadState()

    data class Downloading(val progress: Float) : EpisodeDownloadState()

    data class Ready(val localPath: String) : EpisodeDownloadState()

    data object Failed : EpisodeDownloadState()
}

interface PodcastRepository {
    suspend fun shows(): List<PodcastShow>

    suspend fun enqueue(episode: Episode)

    suspend fun savePosition(position: PlaybackPosition)
}

class LibraryPodcastRepository(
    private val library: LibraryRepository,
) : PodcastRepository {
    private val shows = linkedMapOf<String, PodcastShow>()
    private val queue = mutableListOf<String>()

    override suspend fun shows(): List<PodcastShow> = shows.values.toList()

    override suspend fun enqueue(episode: Episode) {
        if (episode.id !in queue) queue += episode.id
    }

    override suspend fun savePosition(position: PlaybackPosition) {
        library.upsertPlayback(position)
    }

    fun queueSnapshot(): PlaybackQueue = PlaybackQueue(queue.toList())
}

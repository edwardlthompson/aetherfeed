package org.aetherfeed.app.podcasts

import org.aetherfeed.app.domain.Episode
import java.io.File

class EpisodeDownloader(
    private val storageRoot: File,
) {
    init {
        storageRoot.mkdirs()
    }

    private val states = linkedMapOf<String, EpisodeDownloadState>()

    fun stateFor(episodeId: String): EpisodeDownloadState =
        states[episodeId] ?: EpisodeDownloadState.None

    fun snapshot(): Map<String, EpisodeDownloadState> = states.toMap()

    suspend fun enqueue(episode: Episode) {
        if (stateFor(episode.id) is EpisodeDownloadState.Ready) return
        states[episode.id] = EpisodeDownloadState.Queued
    }

    /** Stub completion: no network; writes an empty file under [storageRoot]. */
    suspend fun completeQueued() {
        states
            .filterValues { it is EpisodeDownloadState.Queued }
            .keys
            .toList()
            .forEach { id -> completeOne(id) }
    }

    private fun completeOne(episodeId: String) {
        states[episodeId] = EpisodeDownloadState.Downloading(0.5f)
        val file = File(storageRoot, "$episodeId.mp3")
        file.parentFile?.mkdirs()
        file.writeBytes(byteArrayOf())
        states[episodeId] = EpisodeDownloadState.Ready(file.absolutePath)
    }
}

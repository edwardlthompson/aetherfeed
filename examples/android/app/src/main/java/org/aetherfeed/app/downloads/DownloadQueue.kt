package org.aetherfeed.app.downloads

import org.aetherfeed.app.domain.Episode

/** In-memory FIFO episode queue. Files land only in [AppPrivateEpisodeStore]. */
class DownloadQueue(
    private val store: AppPrivateEpisodeStore,
    private val fetcher: EpisodeFetcher,
    val flags: DownloadFlags = DownloadFlags(),
    private val onWifi: () -> Boolean = { true },
) {
    private val jobs = linkedMapOf<String, DownloadJob>()

    fun enqueue(episode: Episode): DownloadJob {
        require(episode.id.isNotBlank()) { "episodeId" }
        require(episode.enclosureUrl.isNotBlank()) { "enclosureUrl" }
        val existing = jobs[episode.id]
        if (existing != null && existing.status != DownloadStatus.Failed) return existing
        val job = DownloadJob(episode.id, episode.enclosureUrl)
        jobs[episode.id] = job
        return job
    }

    fun snapshot(): List<DownloadJob> = jobs.values.toList()

    fun job(episodeId: String): DownloadJob? = jobs[episodeId]

    fun canDownloadNow(): Boolean = !flags.wifiOnly || onWifi()

    fun shouldAutoEnqueue(): Boolean = flags.autoDownload

    suspend fun processNext(): DownloadJob? {
        if (!canDownloadNow()) return null
        val next = jobs.values.firstOrNull { it.status == DownloadStatus.Queued } ?: return null
        jobs[next.episodeId] = next.copy(status = DownloadStatus.InProgress)
        val updated = runCatching {
            complete(next, fetcher.fetch(next.enclosureUrl))
        }.fold(
            onSuccess = { it },
            onFailure = { err -> fail(next, err) },
        )
        jobs[next.episodeId] = updated
        return updated
    }

    fun deleteDownloaded(episodeId: String) {
        store.delete(episodeId)
        jobs.remove(episodeId)
    }

    private fun complete(job: DownloadJob, bytes: ByteArray): DownloadJob {
        val file = store.write(job.episodeId, bytes)
        return job.copy(status = DownloadStatus.Completed, localPath = file.absolutePath, error = null)
    }

    private fun fail(job: DownloadJob, err: Throwable): DownloadJob {
        store.delete(job.episodeId)
        return job.copy(
            status = DownloadStatus.Failed,
            localPath = null,
            error = err.message ?: "fetch failed",
        )
    }
}

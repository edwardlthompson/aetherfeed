package org.aetherfeed.app.downloads

/** Wi-Fi and auto-download preferences. Defaults conserve metered data. */
data class DownloadFlags(
    val wifiOnly: Boolean = true,
    val autoDownload: Boolean = false,
)

enum class DownloadStatus { Queued, InProgress, Completed, Failed }

data class DownloadJob(
    val episodeId: String,
    val enclosureUrl: String,
    val status: DownloadStatus = DownloadStatus.Queued,
    val localPath: String? = null,
    val error: String? = null,
)

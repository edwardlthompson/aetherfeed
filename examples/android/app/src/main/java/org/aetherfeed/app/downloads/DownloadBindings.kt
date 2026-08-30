package org.aetherfeed.app.downloads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.io.File

fun createAppDownloadQueue(
    filesDir: File,
    onWifi: () -> Boolean,
): DownloadQueue =
    DownloadQueue(
        store = AppPrivateEpisodeStore(filesDir),
        fetcher = HttpEpisodeFetcher(),
        onWifi = onWifi,
    )

fun isUnmeteredNetwork(context: Context): Boolean {
    val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
}

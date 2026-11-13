package org.aetherfeed.app.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import org.aetherfeed.app.domain.NotificationChannelPref

/**
 * Creates or updates system [NotificationChannel] rows for news and optional podcasts,
 * driven by [NotificationChannelPref] / [UnreadTotal.channels].
 */
class NotificationChannelManager(
    private val context: Context,
) {
    fun ensureChannels(prefs: List<NotificationChannelPref>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val effective =
            if (prefs.isEmpty()) {
                defaultNotificationChannels(context)
            } else {
                prefs
            }
        for (pref in effective.filter { it.enabled }) {
            val channel =
                NotificationChannel(
                    pref.id,
                    pref.label,
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = pref.label
                }
            manager.createNotificationChannel(channel)
        }
    }

    suspend fun ensureFrom(unread: UnreadTotal) {
        ensureChannels(unread.channels())
    }
}

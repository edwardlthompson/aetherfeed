package org.aetherfeed.app.notify

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.aetherfeed.app.R
import org.aetherfeed.shared.domain.notify.ArticleNotice

/** Posts local article notices on enabled news/podcast channels (no booru). */
class ArticleNotificationPoster(
    private val context: Context,
) {
    private val channelManager = NotificationChannelManager(context)
    private var nextId = 4000

    fun post(notice: ArticleNotice) {
        channelManager.ensureChannels(defaultNotificationChannels(context))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        val notification =
            NotificationCompat.Builder(context, notice.channelId)
                .setSmallIcon(R.drawable.ic_brand_mark)
                .setContentTitle(notice.title)
                .setContentText(notice.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notice.body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        manager.notify(nextId++, notification)
    }
}
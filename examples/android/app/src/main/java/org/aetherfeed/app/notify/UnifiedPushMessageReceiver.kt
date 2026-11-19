package org.aetherfeed.app.notify

import android.content.Context
import org.aetherfeed.shared.domain.notify.ArticleNotice
import org.aetherfeed.shared.domain.notify.NotifyChannelIds
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage

/** Remote wake from a FOSS UnifiedPush distributor — local notice only (no FCM). */
class UnifiedPushMessageReceiver : MessagingReceiver() {
    override fun onMessage(
        context: Context,
        message: PushMessage,
        instance: String,
    ) {
        val poster = ArticleNotificationPoster(context.applicationContext)
        val text = message.content.toString(Charsets.UTF_8).trim()
        val body = if (text.isBlank()) "New content available" else text
        poster.post(
            ArticleNotice(
                channelId = NotifyChannelIds.NEWS,
                title = "AetherFeed",
                body = body,
            ),
        )
    }

    override fun onNewEndpoint(
        context: Context,
        endpoint: PushEndpoint,
        instance: String,
    ) = Unit

    override fun onRegistrationFailed(
        context: Context,
        reason: FailedReason,
        instance: String,
    ) = Unit

    override fun onUnregistered(
        context: Context,
        instance: String,
    ) = Unit
}
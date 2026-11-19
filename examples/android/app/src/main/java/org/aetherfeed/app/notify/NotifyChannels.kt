package org.aetherfeed.app.notify

import android.content.Context
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.NotificationChannelPref

import org.aetherfeed.shared.domain.notify.NotifyChannelIds as SharedNotifyChannelIds

/** Stable notification channel ids — keep aligned with [NotificationChannelPref.id]. */
object NotifyChannelIds {
    const val NEWS = SharedNotifyChannelIds.NEWS
    const val PODCAST = SharedNotifyChannelIds.PODCAST
}

fun defaultNotificationChannels(context: Context): List<NotificationChannelPref> =
    listOf(
        NotificationChannelPref(
            id = NotifyChannelIds.NEWS,
            module = ModuleKind.News,
            enabled = true,
            label = context.getString(R.string.nav_news),
        ),
        NotificationChannelPref(
            id = NotifyChannelIds.PODCAST,
            module = ModuleKind.Podcast,
            enabled = true,
            label = context.getString(R.string.nav_podcasts),
        ),
    )

/** Enabled channel ids from user prefs (e.g. [UnreadTotal.channels]). */
fun enabledChannelIds(prefs: List<NotificationChannelPref>): List<String> =
    prefs.filter { it.enabled }.map { it.id }

fun formatUnreadTrayLabel(count: Int): String = org.aetherfeed.shared.domain.notify.formatUnreadTrayLabel(count)

package org.aetherfeed.app.notify

import android.content.Context
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.NotificationChannelPref

/** Stable notification channel ids — keep aligned with [NotificationChannelPref.id]. */
object NotifyChannelIds {
    const val NEWS = "aetherfeed_news"
    const val PODCAST = "aetherfeed_podcast"
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

/**
 * Desktop tray tooltip contract — keep identical to `examples/desktop/src-tauri/src/lib.rs`.
 */
fun formatUnreadTrayLabel(count: Int): String = "AetherFeed — $count unread"

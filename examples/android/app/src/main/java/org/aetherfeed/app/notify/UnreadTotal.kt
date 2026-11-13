package org.aetherfeed.app.notify

import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.NotificationChannelPref

/**
 * Locked unread-total API for Android widgets and desktop tray.
 * Callers must not invent a second counter; always go through [UnreadTotal].
 */
interface UnreadTotal {
    suspend fun current(): Int

    suspend fun channels(): List<NotificationChannelPref>
}

class LibraryUnreadTotal(
    private val library: LibraryRepository,
    private val channelStore: () -> List<NotificationChannelPref> = { emptyList() },
) : UnreadTotal {
    override suspend fun current(): Int = library.unreadCount()

    override suspend fun channels(): List<NotificationChannelPref> = channelStore()
}

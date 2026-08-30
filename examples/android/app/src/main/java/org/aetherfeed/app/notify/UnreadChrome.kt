package org.aetherfeed.app.notify

/** Shared unread total used by the Android widget and desktop tray. */
fun unreadTotal(states: Collection<org.aetherfeed.app.domain.ReadState>): Int =
    org.aetherfeed.app.domain.totalUnread(states)

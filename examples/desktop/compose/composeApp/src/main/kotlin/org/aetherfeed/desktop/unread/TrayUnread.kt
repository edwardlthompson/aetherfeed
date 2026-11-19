package org.aetherfeed.desktop.unread

import org.aetherfeed.shared.domain.ReadStatus
import org.aetherfeed.shared.domain.unread.totalUnread as sharedTotalUnread

/**
 * Desktop tray tooltip contract — keep identical to
 * `examples/android/.../notify/NotifyChannels.kt` and Tauri `set_unread_total`.
 */
fun formatUnreadTrayLabel(count: Int): String = org.aetherfeed.shared.domain.notify.formatUnreadTrayLabel(count)

/** Delegates to `shared/domain` SoT; kept for desktop import stability. */
fun totalUnread(statuses: Collection<ReadStatus>): Int = sharedTotalUnread(statuses)
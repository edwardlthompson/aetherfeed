package org.aetherfeed.shared.domain.unread

import org.aetherfeed.shared.domain.ReadStatus

/** Count items that are not fully read — shared by Android, desktop tray, iOS widget file. */
fun totalUnread(statuses: Collection<ReadStatus>): Int =
    statuses.count { it != ReadStatus.Read }
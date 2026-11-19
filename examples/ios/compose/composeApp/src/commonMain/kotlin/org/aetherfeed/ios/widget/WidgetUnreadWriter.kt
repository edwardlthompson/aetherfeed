package org.aetherfeed.ios.widget

import org.aetherfeed.shared.domain.unread.WidgetUnreadSnapshot
import org.aetherfeed.shared.domain.unread.encodeWidgetUnreadSnapshot

/** Describes where the app process writes the widget snapshot (main app only, not extension). */
data class WidgetUnreadWritePlan(
    val appGroupId: String,
    val relativeFilename: String,
    val snapshot: WidgetUnreadSnapshot,
)

fun WidgetUnreadWritePlan.encodedUtf8(): String = encodeWidgetUnreadSnapshot(snapshot)
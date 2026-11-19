package org.aetherfeed.shared.domain.unread

/**
 * WidgetKit / App Group file contract (TECH_DECISION §5, DESIGN_BRIEF §8).
 * The main app writes this JSON; the WidgetKit extension reads count only (no vault access).
 */
const val IOS_APP_GROUP_ID = "group.org.aetherfeed.app"

/** Relative path inside the App Group container (UTF-8 JSON). */
const val IOS_WIDGET_UNREAD_FILENAME = "unread_widget_snapshot.json"

const val WIDGET_UNREAD_JSON_VERSION = 1

data class WidgetUnreadSnapshot(
    val version: Int = WIDGET_UNREAD_JSON_VERSION,
    val unreadTotal: Int,
    val updatedAtEpochMs: Long,
) {
    init {
        require(version == WIDGET_UNREAD_JSON_VERSION) { "unsupported widget snapshot version" }
        require(unreadTotal >= 0) { "unreadTotal must be non-negative" }
    }

    /** Display line — matches Android `unread_total` string: `Unread: %d`. */
    fun widgetLine(): String = "Unread: $unreadTotal"
}

/** Minimal JSON encoder (no kotlinx on JVM domain module). */
fun encodeWidgetUnreadSnapshot(snapshot: WidgetUnreadSnapshot): String =
    """{"version":${snapshot.version},"unreadTotal":${snapshot.unreadTotal},"updatedAtEpochMs":${snapshot.updatedAtEpochMs}}"""

fun decodeWidgetUnreadSnapshot(json: String): WidgetUnreadSnapshot {
    val version = json.intField("version")
    val total = json.intField("unreadTotal")
    val updated = json.longField("updatedAtEpochMs")
    return WidgetUnreadSnapshot(version = version, unreadTotal = total, updatedAtEpochMs = updated)
}

private fun String.intField(key: String): Int {
    val pattern = """"$key"\s*:\s*(-?\d+)""".toRegex()
    return pattern.find(this)?.groupValues?.get(1)?.toInt()
        ?: error("missing $key in widget snapshot")
}

private fun String.longField(key: String): Long {
    val pattern = """"$key"\s*:\s*(-?\d+)""".toRegex()
    return pattern.find(this)?.groupValues?.get(1)?.toLong()
        ?: error("missing $key in widget snapshot")
}
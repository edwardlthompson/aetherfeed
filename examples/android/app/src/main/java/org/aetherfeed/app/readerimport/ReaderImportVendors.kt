package org.aetherfeed.app.readerimport

/** Vendor-specific member lookup for Takeout and Inoreader backups. */

fun takeoutSubscriptionsXml(members: Map<String, String>): String? =
    members.entries.firstOrNull { it.key.endsWith("subscriptions.xml", ignoreCase = true) }?.value

fun takeoutStarredJson(members: Map<String, String>): String? =
    members.entries.firstOrNull { it.key.endsWith("starred.json", ignoreCase = true) }?.value

fun inoreaderBackupJson(members: Map<String, String>): String? =
    members.entries.firstOrNull { keyValue ->
        val key = keyValue.key.lowercase()
        key.contains("inoreader") && key.endsWith(".json")
    }?.value

fun readerImportSummary(result: ReaderImportResult): String =
    "added=${result.feedsAdded} news=${result.newsAdded} podcasts=${result.podcastsAdded} skipped=${result.feedsSkipped} stars=${result.starsApplied} errors=${result.errors.size}"

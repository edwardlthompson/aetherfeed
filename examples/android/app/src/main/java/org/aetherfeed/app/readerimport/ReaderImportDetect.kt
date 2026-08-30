package org.aetherfeed.app.readerimport

fun detectReaderVendor(file: ReaderImportFile): ReaderVendor {
    val name = file.name.lowercase()
    val members = file.members.keys.joinToString(" ").lowercase()
    val blob = (file.text ?: file.members.values.joinToString("\n")).take(4000).lowercase()
    val hay = "$name $members $blob"
    if (
        name.contains("subscriptions.xml") ||
        name.contains("starred.json") ||
        members.contains("subscriptions.xml") ||
        Regex("takeout|greader|google.?reader").containsMatchIn(hay)
    ) {
        return ReaderVendor.GoogleReader
    }
    if (hay.contains("inoreader")) return ReaderVendor.Inoreader
    if (Regex("<opml\\b").containsMatchIn(blob) || name.endsWith(".opml") || members.contains(".opml")) {
        return ReaderVendor.Opml
    }
    return ReaderVendor.Unknown
}

fun collectImportTexts(file: ReaderImportFile): List<String> {
    if (file.members.isNotEmpty()) {
        val picked = file.members.filterKeys { key ->
            Regex("\\.(xml|opml|json)$", RegexOption.IGNORE_CASE).containsMatchIn(key) ||
                Regex("subscriptions|starred|inoreader", RegexOption.IGNORE_CASE).containsMatchIn(key)
        }
        return (if (picked.isNotEmpty()) picked else file.members).values.toList()
    }
    return listOfNotNull(file.text)
}

fun isHardParseFailure(parsed: ReaderImportParsed): Boolean =
    parsed.outlines.isEmpty() && parsed.stars.isEmpty()

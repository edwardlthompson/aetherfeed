package org.aetherfeed.app.readerimport

import org.aetherfeed.app.news.OpmlOutline

private fun outlineAttr(tag: String, name: String): String? =
    Regex("""$name\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE).find(tag)?.groupValues?.getOrNull(1)

fun parseOpmlOutlines(xml: String): List<OpmlOutline> {
    val tokens = Regex("</outline>|<outline\\b[^>]*/?>", setOf(RegexOption.IGNORE_CASE)).findAll(xml)
    val stack = ArrayDeque<String>()
    val outlines = mutableListOf<OpmlOutline>()
    for (match in tokens) {
        val token = match.value
        if (token.startsWith("</", ignoreCase = true)) {
            if (stack.isNotEmpty()) stack.removeLast()
            continue
        }
        val xmlUrl = outlineAttr(token, "xmlUrl")
        val title = outlineAttr(token, "title") ?: outlineAttr(token, "text") ?: xmlUrl ?: "Untitled"
        val folder = stack.asReversed().firstOrNull { it.isNotEmpty() }
        if (xmlUrl != null) {
            outlines.add(
                OpmlOutline(
                    title = title,
                    xmlUrl = xmlUrl,
                    htmlUrl = outlineAttr(token, "htmlUrl"),
                    folder = folder,
                    type = outlineAttr(token, "type"),
                ),
            )
        }
        if (!token.trim().endsWith("/>")) stack.addLast(if (xmlUrl != null) "" else title)
    }
    return outlines
}

private fun jsonField(blob: String, key: String): String? =
    Regex(""""$key"\s*:\s*"([^"]*)"""").find(blob)?.groupValues?.getOrNull(1)

fun parseReaderJson(text: String): Triple<List<OpmlOutline>, List<ReaderImportStar>, List<ReaderImportRead>> {
    if (text.isBlank() || (!text.trimStart().startsWith("{") && !text.trimStart().startsWith("["))) {
        error("Malformed JSON backup")
    }
    val starred = text.contains("starred", ignoreCase = true)
    val outlines = mutableListOf<OpmlOutline>()
    val stars = mutableListOf<ReaderImportStar>()
    val reads = mutableListOf<ReaderImportRead>()
    val objects = Regex("""\{[^{}]+\}""").findAll(text).map { it.value }
    for (obj in objects) {
        val url = jsonField(obj, "href") ?: jsonField(obj, "feedUrl") ?: jsonField(obj, "xmlUrl")
            ?: jsonField(obj, "url") ?: continue
        val title = jsonField(obj, "title") ?: url
        val htmlUrl = jsonField(obj, "htmlUrl")
        val asStar = starred || obj.contains("canonical") || (obj.contains("\"href\"") && !obj.contains("feedUrl"))
        if (asStar) {
            stars.add(ReaderImportStar(url, title))
        } else {
            outlines.add(
                OpmlOutline(
                    title = title,
                    xmlUrl = url,
                    htmlUrl = htmlUrl,
                    folder = jsonField(obj, "folder"),
                    type = jsonField(obj, "kind") ?: jsonField(obj, "type"),
                ),
            )
        }
    }
    return Triple(outlines, stars, reads)
}

fun parseReaderImport(file: ReaderImportFile): ReaderImportParsed {
    val vendor = detectReaderVendor(file)
    val texts = collectImportTexts(file)
    if (texts.isEmpty() || texts.all { it.isBlank() }) {
        return ReaderImportParsed(
            vendor, emptyList(), emptyList(), emptyList(),
            listOf(ReaderImportError(ReaderImportErrorCode.Empty, "Import file is empty")),
        )
    }
    val outlines = mutableListOf<OpmlOutline>()
    val stars = mutableListOf<ReaderImportStar>()
    val reads = mutableListOf<ReaderImportRead>()
    val errors = mutableListOf<ReaderImportError>()
    for (raw in texts) {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) continue
        if (trimmed.startsWith("<") || Regex("<opml\\b", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) {
            runCatching { outlines.addAll(parseOpmlOutlines(trimmed)) }
                .onFailure { errors.add(ReaderImportError(ReaderImportErrorCode.Malformed, "Malformed OPML")) }
            continue
        }
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            runCatching {
                val extra = parseReaderJson(trimmed)
                outlines.addAll(extra.first)
                stars.addAll(extra.second)
                reads.addAll(extra.third)
            }.onFailure { errors.add(ReaderImportError(ReaderImportErrorCode.Malformed, "Malformed JSON backup")) }
        }
    }
    val parsed = ReaderImportParsed(vendor, outlines, stars, reads, errors)
    if (isHardParseFailure(parsed) && errors.isEmpty()) {
        errors.add(ReaderImportError(ReaderImportErrorCode.Malformed, "No subscriptions found"))
    }
    return parsed.copy(errors = errors)
}

package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind

data class NewsOpmlFeed(val feed: Feed, val folder: String?)

private fun outlineAttr(tag: String, name: String): String? =
    Regex("""$name\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE).find(tag)?.groupValues?.getOrNull(1)

fun outlinesFromOpml(xml: String): List<OpmlOutline> {
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

fun parseOpmlFeeds(xml: String, now: Long): List<NewsOpmlFeed> =
    flattenOpml(outlinesFromOpml(xml)).mapNotNull { outline ->
        val url = outline.xmlUrl?.trim().orEmpty()
        if (url.isEmpty()) {
            null
        } else {
            NewsOpmlFeed(
                Feed("feed:$url", outline.title.ifBlank { url }, url, ModuleKind.News, outline.htmlUrl, now),
                outline.folder,
            )
        }
    }

fun exportOpmlXml(feeds: List<Feed>, folderOf: (Feed) -> String): String {
    val body = feeds.groupBy(folderOf).toList().sortedBy { it.first }.joinToString("") { (folder, rows) ->
        val kids = rows.joinToString("") { feed ->
            """<outline text="${escapeOpml(feed.title)}" xmlUrl="${escapeOpml(feed.url)}"/>"""
        }
        """<outline text="${escapeOpml(folder)}">$kids</outline>"""
    }
    return """<?xml version="1.0"?><opml version="1.0"><body>$body</body></opml>"""
}

private fun escapeOpml(value: String): String =
    value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;")

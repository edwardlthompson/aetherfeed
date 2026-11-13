package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind

/**
 * Locked parse entry points. JSON Feed 1.1, RSS 2.0, Atom, and OPML outline extraction.
 */
fun parseFeed(
    bytes: ByteArray,
    contentType: String,
    baseUrl: String,
    nowMs: Long = System.currentTimeMillis(),
): FeedDocument {
    if (bytes.isEmpty()) {
        throw NewsParseError(NewsParseCode.Empty, "empty feed body")
    }
    val text = bytes.toString(Charsets.UTF_8).trimStart()
    val type = contentType.lowercase()
    return when {
        type.contains("json") || text.startsWith("{") -> parseJsonFeed(text, baseUrl, nowMs)
        type.contains("xml") || text.startsWith("<") -> parseXmlFeed(text, baseUrl, nowMs)
        else -> throw NewsParseError(NewsParseCode.Malformed, "unrecognized feed payload")
    }
}

fun parseOpml(bytes: ByteArray, nowMs: Long = System.currentTimeMillis()): List<Feed> {
    if (bytes.isEmpty()) {
        throw NewsParseError(NewsParseCode.Empty, "empty OPML")
    }
    val xml = bytes.toString(Charsets.UTF_8)
    val feeds = mutableListOf<Feed>()
    val outline = Regex("""<outline\b([^>]*)/?>""", RegexOption.IGNORE_CASE)
    val attr = xmlAttributePattern
    outline.findAll(xml).forEach { match ->
        val attrs = attr.findAll(match.groupValues[1]).associate { it.groupValues[1] to it.groupValues[2] }
        val xmlUrl = attrs["xmlUrl"] ?: return@forEach
        val title = attrs["title"] ?: attrs["text"] ?: xmlUrl
        feeds +=
            Feed(
                id = xmlUrl,
                title = title,
                url = xmlUrl,
                kind = ModuleKind.News,
                siteUrl = attrs["htmlUrl"],
                updatedAt = nowMs,
            )
    }
    if (feeds.isEmpty()) {
        throw NewsParseError(NewsParseCode.Malformed, "no OPML outlines with xmlUrl")
    }
    return feeds
}

fun exportOpml(feeds: List<Feed>): ByteArray {
    val outlines =
        feeds.joinToString("\n") { feed ->
            val site = feed.siteUrl?.let { """ htmlUrl="${escapeXml(it)}"""" }.orEmpty()
            """  <outline type="rss" text="${escapeXml(feed.title)}" title="${escapeXml(feed.title)}" xmlUrl="${escapeXml(feed.url)}"$site />"""
        }
    val body =
        """
        |<?xml version="1.0" encoding="UTF-8"?>
        |<opml version="2.0">
        |<head><title>AetherFeed</title></head>
        |<body>
        |$outlines
        |</body>
        |</opml>
        """.trimMargin()
    return body.toByteArray(Charsets.UTF_8)
}

internal val xmlAttributePattern = Regex("""(\w+)\s*=\s*"([^"]*)"""")

internal fun escapeXml(value: String): String =
    value
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

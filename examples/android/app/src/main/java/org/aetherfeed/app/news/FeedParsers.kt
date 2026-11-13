package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.json.JSONObject

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

private val xmlAttributePattern = Regex("""(\w+)\s*=\s*"([^"]*)"""")

private fun parseXmlFeed(text: String, baseUrl: String, nowMs: Long): FeedDocument {
    val lower = text.lowercase()
    return when {
        lower.contains("<feed") -> parseAtomFeed(text, baseUrl, nowMs)
        lower.contains("<rss") -> parseRssFeed(text, baseUrl, nowMs)
        else -> throw NewsParseError(NewsParseCode.Malformed, "unrecognized XML feed")
    }
}

private fun parseRssFeed(xml: String, baseUrl: String, nowMs: Long): FeedDocument {
    val channel = channelBlock.find(xml)?.groupValues?.get(1) ?: xml
    val channelTitle = firstTagText(channel, "title") ?: baseUrl
    val channelLink = firstTagText(channel, "link") ?: baseUrl
    val feed =
        Feed(
            id = channelLink,
            title = channelTitle,
            url = baseUrl.ifBlank { channelLink },
            kind = ModuleKind.News,
            siteUrl = channelLink,
            updatedAt = nowMs,
        )
    val articles =
        itemBlock.findAll(channel).mapNotNull { match ->
            val block = match.groupValues[1]
            val title = firstTagText(block, "title") ?: return@mapNotNull null
            val link = firstTagText(block, "link") ?: ""
            val guid = firstTagText(block, "guid") ?: link.ifBlank { title }
            if (guid.isBlank()) return@mapNotNull null
            val url = link.ifBlank { guid }
            Article(
                id = guid,
                feedId = feed.id,
                title = title,
                url = url,
                publishedAt = null,
                summary = firstTagText(block, "description"),
                localPath = null,
            )
        }.toList()
    if (articles.isEmpty()) {
        throw NewsParseError(NewsParseCode.Empty, "RSS feed had no usable items")
    }
    return FeedDocument(feeds = listOf(feed), articles = articles)
}

private fun parseAtomFeed(xml: String, baseUrl: String, nowMs: Long): FeedDocument {
    val feedTitle = firstTagText(xml, "title") ?: baseUrl
    val feedLink = firstLinkHref(xml) ?: baseUrl
    val feedId = firstTagText(xml, "id") ?: feedLink
    val feed =
        Feed(
            id = feedId,
            title = feedTitle,
            url = baseUrl.ifBlank { feedLink },
            kind = ModuleKind.News,
            siteUrl = feedLink,
            updatedAt = nowMs,
        )
    val articles =
        entryBlock.findAll(xml).mapNotNull { match ->
            val block = match.groupValues[1]
            val id = firstTagText(block, "id") ?: return@mapNotNull null
            val title = firstTagText(block, "title") ?: id
            val url = firstLinkHref(block) ?: id
            Article(
                id = id,
                feedId = feed.id,
                title = title,
                url = url,
                publishedAt = null,
                summary = firstTagText(block, "summary") ?: firstTagText(block, "content"),
                localPath = null,
            )
        }.toList()
    if (articles.isEmpty()) {
        throw NewsParseError(NewsParseCode.Empty, "Atom feed had no usable entries")
    }
    return FeedDocument(feeds = listOf(feed), articles = articles)
}

private val channelBlock =
    Regex("""<channel\b[^>]*>(.*?)</channel>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

private val itemBlock =
    Regex("""<item\b[^>]*>(.*?)</item>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

private val entryBlock =
    Regex("""<entry\b[^>]*>(.*?)</entry>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

private fun tagPattern(tag: String) =
    Regex("""<$tag\b[^>]*>(.*?)</$tag>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

private fun firstTagText(block: String, tag: String): String? =
    tagPattern(tag).find(block)?.groupValues?.get(1)?.let(::decodeXmlText)?.ifBlank { null }

private fun firstLinkHref(block: String): String? {
    val links = Regex("""<link\b([^>]*)/?>""", RegexOption.IGNORE_CASE).findAll(block)
    var fallback: String? = null
    links.forEach { match ->
        val attrs = xmlAttributePattern.findAll(match.groupValues[1]).associate { it.groupValues[1] to it.groupValues[2] }
        val href = attrs["href"] ?: return@forEach
        val rel = attrs["rel"]?.lowercase()
        if (rel == null || rel == "alternate") return href
        if (fallback == null) fallback = href
    }
    return fallback ?: firstTagText(block, "link")
}

private fun decodeXmlText(raw: String): String {
    var text = raw.trim()
    val cdata = Regex("""<!\[CDATA\[(.*?)\]\]>""", RegexOption.DOT_MATCHES_ALL)
    text = cdata.replace(text) { it.groupValues[1] }
    text = text.replace(Regex("<[^>]+>"), " ")
    return text
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun parseJsonFeed(text: String, baseUrl: String, nowMs: Long): FeedDocument {
    val root =
        try {
            JSONObject(text)
        } catch (_: Exception) {
            throw NewsParseError(NewsParseCode.Malformed, "invalid JSON Feed")
        }
    val title = root.optString("title").ifBlank { baseUrl }
    val home = root.optString("home_page_url").ifBlank { null }
    val feedUrl = root.optString("feed_url").ifBlank { baseUrl }
    val feed =
        Feed(
            id = feedUrl,
            title = title,
            url = feedUrl,
            kind = ModuleKind.News,
            siteUrl = home,
            updatedAt = nowMs,
        )
    val items = root.optJSONArray("items") ?: throw NewsParseError(NewsParseCode.Empty, "no items")
    val articles =
        buildList {
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val id = item.optString("id").ifBlank { item.optString("url") }
                if (id.isBlank()) continue
                add(
                    Article(
                        id = id,
                        feedId = feed.id,
                        title = item.optString("title").ifBlank { id },
                        url = item.optString("url").ifBlank { id },
                        publishedAt = null,
                        summary = item.optString("summary").ifBlank { null },
                        localPath = null,
                    ),
                )
            }
        }
    if (articles.isEmpty()) {
        throw NewsParseError(NewsParseCode.Empty, "JSON Feed had no usable items")
    }
    return FeedDocument(feeds = listOf(feed), articles = articles)
}

private fun escapeXml(value: String): String =
    value
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

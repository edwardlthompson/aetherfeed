package org.aetherfeed.app.news

private fun decodeXml(raw: String): String =
    raw.replace(Regex("""<!\[CDATA\[([\s\S]*?)]]>""", RegexOption.IGNORE_CASE), "$1")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .trim()

private fun inner(tag: String, xml: String): String? {
    val match = Regex("<$tag\\b[^>]*>([\\s\\S]*?)</$tag>", RegexOption.IGNORE_CASE).find(xml)
    return match?.groupValues?.get(1)?.let(::decodeXml)
}

private fun prelude(xml: String, itemTag: String): String {
    val start = Regex("<$itemTag\\b", RegexOption.IGNORE_CASE).find(xml)?.range?.first ?: return xml
    return xml.substring(0, start)
}

private fun hrefOf(xml: String): String =
    Regex("""href\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(xml)?.groupValues?.get(1).orEmpty()

fun parseRssFeed(body: String): ParsedFeed {
    val channel = Regex("<channel\\b[^>]*>([\\s\\S]*)</channel>", RegexOption.IGNORE_CASE)
        .find(body)?.groupValues?.get(1) ?: body
    val head = prelude(channel, "item")
    val items = Regex("<item\\b[^>]*>([\\s\\S]*?)</item>", RegexOption.IGNORE_CASE).findAll(channel).mapIndexed { index, match ->
        val block = match.groupValues[1]
        val url = inner("link", block).orEmpty()
        ParsedArticle(
            id = inner("guid", block)?.ifBlank { null } ?: url.ifBlank { "item-$index" },
            title = inner("title", block)?.ifBlank { "Untitled" } ?: "Untitled",
            url = url,
            summary = inner("description", block),
            contentHtml = inner("content:encoded", block),
        )
    }.toList()
    return ParsedFeed(
        format = FeedFormat.Rss,
        title = inner("title", head)?.ifBlank { "Untitled feed" } ?: "Untitled feed",
        siteUrl = inner("link", head),
        items = items,
    )
}

fun parseAtomFeed(body: String): ParsedFeed {
    val head = prelude(body, "entry")
    val items = Regex("<entry\\b[^>]*>([\\s\\S]*?)</entry>", RegexOption.IGNORE_CASE).findAll(body).mapIndexed { index, match ->
        val block = match.groupValues[1]
        val url = hrefOf(block).ifBlank { inner("link", block).orEmpty() }
        ParsedArticle(
            id = inner("id", block)?.ifBlank { null } ?: url.ifBlank { "entry-$index" },
            title = inner("title", block)?.ifBlank { "Untitled" } ?: "Untitled",
            url = url,
            summary = inner("summary", block),
            contentHtml = inner("content", block),
        )
    }.toList()
    return ParsedFeed(
        format = FeedFormat.Atom,
        title = inner("title", head)?.ifBlank { "Untitled feed" } ?: "Untitled feed",
        siteUrl = hrefOf(head).ifBlank { inner("link", head) },
        items = items,
    )
}

fun parseFeedBody(body: String): ParsedFeed = when (sniffFeedFormat(body)) {
    FeedFormat.JsonFeed -> parseJsonFeed(body)
    FeedFormat.Rss -> parseRssFeed(body)
    FeedFormat.Atom -> parseAtomFeed(body)
    null -> throw NewsRefreshError.ParseFailed("Unknown feed format")
}

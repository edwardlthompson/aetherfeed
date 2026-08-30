package org.aetherfeed.app.news

import org.json.JSONObject

fun sniffFeedFormat(body: String): FeedFormat? {
    val trimmed = body.trim()
    if (trimmed.startsWith("{")) {
        return try {
            val version = JSONObject(trimmed).optString("version")
            if (version.contains("jsonfeed")) FeedFormat.JsonFeed else null
        } catch (_: Exception) {
            null
        }
    }
    if (trimmed.contains("<rss", ignoreCase = true)) return FeedFormat.Rss
    if (trimmed.contains("<feed") && trimmed.contains("http://www.w3.org/2005/Atom")) {
        return FeedFormat.Atom
    }
    return null
}

fun parseJsonFeed(body: String): ParsedFeed {
    val root = JSONObject(body)
    val items = root.optJSONArray("items")
    val parsed = mutableListOf<ParsedArticle>()
    if (items != null) {
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            parsed.add(
                ParsedArticle(
                    id = item.optString("id").ifEmpty { item.optString("url").ifEmpty { "item-$i" } },
                    title = item.optString("title").ifEmpty { "Untitled" },
                    url = item.optString("url"),
                    summary = item.optString("summary").ifEmpty { null },
                    contentHtml = item.optString("content_html").ifEmpty { null },
                ),
            )
        }
    }
    return ParsedFeed(
        format = FeedFormat.JsonFeed,
        title = root.optString("title").ifEmpty { "Untitled feed" },
        siteUrl = root.optString("home_page_url").ifEmpty { null },
        items = parsed,
    )
}

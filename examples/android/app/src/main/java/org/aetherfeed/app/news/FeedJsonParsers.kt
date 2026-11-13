package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.json.JSONObject

internal fun parseJsonFeed(text: String, baseUrl: String, nowMs: Long): FeedDocument {
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

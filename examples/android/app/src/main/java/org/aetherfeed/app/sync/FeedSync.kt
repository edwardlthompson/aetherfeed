package org.aetherfeed.app.sync

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.readerimport.normalizeFeedUrl
import org.json.JSONArray
import org.json.JSONObject

const val DRIVE_FEEDS_NAME = "aetherfeed-feeds.enc"
const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
const val DRIVE_LOOPBACK_PORT = 17890

fun canonicalFeedId(url: String): String = "feed:${normalizeFeedUrl(url)}"

fun canonicalizeFeed(feed: Feed): Feed = feed.copy(id = canonicalFeedId(feed.url))

fun mergeFeedSources(local: List<Feed>, remote: List<Feed>): List<Feed> {
    val byKey = linkedMapOf<String, Feed>()
    for (feed in (local + remote).map(::canonicalizeFeed)) {
        val key = normalizeFeedUrl(feed.url)
        val prev = byKey[key]
        if (prev == null || feed.updatedAt >= prev.updatedAt) byKey[key] = feed
    }
    return byKey.values.toList()
}

fun encodeFeedDocument(feeds: List<Feed>, now: Long): String {
    val array = JSONArray()
    for (feed in feeds.map(::canonicalizeFeed)) {
                val obj = JSONObject()
                    .put("id", feed.id)
                    .put("title", feed.title)
                    .put("url", feed.url)
                    .put("kind", feed.kind.name.lowercase())
                    .put("updatedAt", feed.updatedAt)
                if (feed.siteUrl != null) obj.put("siteUrl", feed.siteUrl)
                array.put(obj)
    }
    return JSONObject().put("version", 1).put("updatedAt", now).put("feeds", array).toString()
}

fun decodeFeedDocument(json: String): List<Feed> {
    val root = JSONObject(json)
    require(root.optInt("version") == 1) { "invalid feed sync document" }
    val feeds = root.optJSONArray("feeds") ?: JSONArray()
    return buildList {
        for (i in 0 until feeds.length()) {
            val obj = feeds.getJSONObject(i)
            add(
                canonicalizeFeed(
                    Feed(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        url = obj.optString("url"),
                        kind = kindOf(obj.optString("kind")),
                        siteUrl = obj.optString("siteUrl").ifBlank { null },
                        updatedAt = obj.optLong("updatedAt"),
                    ),
                ),
            )
        }
    }
}

private fun kindOf(raw: String): ModuleKind = when (raw.lowercase()) {
    "podcast" -> ModuleKind.Podcast
    "booru" -> ModuleKind.Booru
    else -> ModuleKind.News
}

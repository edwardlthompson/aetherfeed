package org.aetherfeed.app.news

import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.domain.Article
import org.json.JSONArray
import org.json.JSONObject

const val ARTICLE_INDEX_KIND = "article-index"

interface ArticleIndexStore {
    fun load(feedId: String): List<Article>

    fun save(feedId: String, rows: List<Article>)
}

object NoopArticleIndex : ArticleIndexStore {
    override fun load(feedId: String): List<Article> = emptyList()

    override fun save(feedId: String, rows: List<Article>) = Unit
}

fun encodeArticleIndex(rows: List<Article>): String {
    val arr = JSONArray()
    for (row in rows) {
        arr.put(
            JSONObject().apply {
                put("id", row.id)
                put("feedId", row.feedId)
                put("title", row.title)
                put("url", row.url)
                put("publishedAt", row.publishedAt ?: JSONObject.NULL)
                put("summary", row.summary ?: JSONObject.NULL)
                put("contentHtml", row.contentHtml ?: JSONObject.NULL)
            },
        )
    }
    return arr.toString()
}

fun decodeArticleIndex(raw: String): List<Article> {
    if (raw.isBlank()) return emptyList()
    val arr = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyList()
    return (0 until arr.length()).mapNotNull { index ->
        val obj = arr.optJSONObject(index) ?: return@mapNotNull null
        val id = obj.optString("id")
        val feedId = obj.optString("feedId")
        if (id.isBlank() || feedId.isBlank()) return@mapNotNull null
        Article(
            id = id,
            feedId = feedId,
            title = obj.optString("title"),
            url = obj.optString("url"),
            publishedAt = if (obj.isNull("publishedAt")) null else obj.optLong("publishedAt"),
            summary = obj.optString("summary").ifBlank { null },
            contentHtml = obj.optString("contentHtml").ifBlank { null },
        )
    }
}

class EncryptedArticleIndex(private val cache: EncryptedCache) : ArticleIndexStore {
    override fun load(feedId: String): List<Article> {
        val raw = cache.read(ARTICLE_INDEX_KIND, feedId)?.toString(Charsets.UTF_8) ?: return emptyList()
        return runCatching { decodeArticleIndex(raw) }.getOrDefault(emptyList())
    }

    override fun save(feedId: String, rows: List<Article>) {
        runCatching { cache.write(ARTICLE_INDEX_KIND, feedId, encodeArticleIndex(rows).toByteArray()) }
    }
}

package org.aetherfeed.app.ui.news

import android.util.Log
import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.applock.SessionPlainCache
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.news.hydrateShared
import org.aetherfeed.app.news.peekAround
import org.aetherfeed.app.news.usableBody

internal data class OpenPaint(
    val html: String?,
    val cached: Set<String>,
    val thumb: String?,
)

internal fun sessionHtml(articleId: String): String? =
    usableBody(SessionPlainCache.utf8("articles", articleId)).takeIf { it.isNotEmpty() }

internal fun immediateReaderHtml(articleId: String, cache: EncryptedCache): String? {
    sessionHtml(articleId)?.let { return it }
    return if (cache.exists("articles", articleId)) null else ""
}

internal suspend fun openArticleBody(
    article: Article,
    cache: EncryptedCache,
    sorted: List<Article>,
    allowNet: Boolean,
    known: String?,
    onImage: (Int, Int) -> Unit,
): OpenPaint {
    val started = System.nanoTime()
    val around = peekAround(cache, sorted, article.id)
    var html = known ?: around[article.id]
    Log.i(
        "AetherFeed",
        "open.peek ${article.id.takeLast(12)} ${(System.nanoTime() - started) / 1_000_000}ms " +
            "hit=${html != null} n=${html?.length ?: 0}",
    )
    var thumb: String? = null
    if (html == null) {
        val hydrateAt = System.nanoTime()
        val result = hydrateShared(article, cache, allowNet, onImage)
        html = result.html.ifEmpty { null }
        thumb = result.thumb
        Log.i(
            "AetherFeed",
            "open.hydrate ${article.id.takeLast(12)} ${(System.nanoTime() - hydrateAt) / 1_000_000}ms " +
                "n=${html?.length ?: 0}",
        )
    }
    return OpenPaint(html, around.keys + article.id, thumb)
}

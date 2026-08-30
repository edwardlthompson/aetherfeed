package org.aetherfeed.app.news

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.domain.Article
import java.util.concurrent.ConcurrentHashMap

private val inflight = ConcurrentHashMap<String, CompletableDeferred<HydratedArticle>>()

suspend fun hydrateShared(
    article: Article,
    cache: EncryptedCache,
    allowNet: Boolean,
    onImage: (Int, Int) -> Unit = { _, _ -> },
): HydratedArticle {
    inflight[article.id]?.let { return it.await() }
    val mine = CompletableDeferred<HydratedArticle>()
    val prior = inflight.putIfAbsent(article.id, mine)
    if (prior != null) return prior.await()
    return try {
        val result = withContext(Dispatchers.IO + NonCancellable) {
            hydrateArticle(article, cache, allowNet, onImage)
        }
        mine.complete(result)
        result
    } catch (error: Throwable) {
        mine.completeExceptionally(error)
        throw error
    } finally {
        inflight.remove(article.id, mine)
    }
}

suspend fun hydrateNeighbors(
    cache: EncryptedCache,
    articles: List<Article>,
    currentId: String?,
    allowNet: Boolean,
): Map<String, String> = withContext(Dispatchers.IO) {
    val out = LinkedHashMap<String, String>()
    for (row in aroundArticles(articles, currentId)) {
        if (row.id == currentId) continue
        peekCachedHtml(cache, row.id)?.let { continue }
        val html = runCatching { hydrateShared(row, cache, allowNet).html }.getOrNull()
        usableBody(html).takeIf { it.isNotEmpty() }?.let { out[row.id] = it }
    }
    out
}

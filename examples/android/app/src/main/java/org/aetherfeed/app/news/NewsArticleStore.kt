package org.aetherfeed.app.news

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.domain.Article

const val FETCHED_KIND = "fetched"
const val READY_KIND = "ready"

fun wasFetched(cache: EncryptedCache, articleId: String): Boolean =
    cache.exists("articles", articleId) || cache.exists(FETCHED_KIND, articleId)

fun isReady(cache: EncryptedCache, articleId: String): Boolean = cache.exists(READY_KIND, articleId)

fun peekArticleHtml(cache: EncryptedCache, articleId: String): String? =
    runCatching { cache.read("articles", articleId)?.toString(Charsets.UTF_8) }.getOrNull()

fun needsHydrate(cache: EncryptedCache, article: Article, readIds: Set<String>): Boolean {
    if (article.id in readIds) return false
    if (cache.exists("articles", article.id) && isReady(cache, article.id)) return false
    if (!cache.exists("articles", article.id)) return !wasFetched(cache, article.id)
    val cached = peekArticleHtml(cache, article.id)
    if (usableBody(cached).isNotEmpty()) {
        markReady(cache, article.id)
        return false
    }
    if (isFeedExcerpt(cached)) return true
    return !wasFetched(cache, article.id)
}

fun markFetched(cache: EncryptedCache, articleId: String) {
    if (cache.exists(FETCHED_KIND, articleId)) return
    runCatching { cache.write(FETCHED_KIND, articleId, byteArrayOf(1)) }
}

fun markReady(cache: EncryptedCache, articleId: String) {
    if (cache.exists(READY_KIND, articleId)) return
    runCatching { cache.write(READY_KIND, articleId, byteArrayOf(1)) }
}

data class HydratedArticle(val html: String, val thumb: String?)

data class CacheProgress(
    val done: Int = 0,
    val total: Int = 0,
    val thumbId: String? = null,
    val thumb: String? = null,
)

suspend fun hydrateArticle(
    article: Article,
    cache: EncryptedCache,
    allowNet: Boolean,
    onImage: (Int, Int) -> Unit = { _, _ -> },
): HydratedArticle = withContext(Dispatchers.IO) {
    val cached = runCatching { cache.read("articles", article.id)?.toString(Charsets.UTF_8) }.getOrNull()
    usableBody(cached).takeIf { it.isNotEmpty() }?.let { body ->
        markReady(cache, article.id)
        val thumb = loadThumb(cache, article.id)
            ?: firstDataThumb(body)?.let(::shrinkThumbDataUrl)?.also { persistThumb(cache, article.id, it) }
        return@withContext HydratedArticle(body, thumb)
    }
    val raw = resolveArticleHtml(
        article,
        if (allowNet) HttpFeedFetcher() else FeedBodyFetcher { _, _ -> "" },
        cached,
    )
    val body = dropNonContentImages(raw)
    val html = if (allowNet && body.isNotEmpty()) {
        cacheArticleImages(body, HttpImageFetcher(), cache, article.id, onImage)
    } else {
        onImage(1, 1)
        body
    }
    if (html.isNotEmpty()) {
        runCatching { cache.write("articles", article.id, html.toByteArray()) }
        stampCachedAt(cache, article.id)
        if (usableBody(html).isNotEmpty()) markReady(cache, article.id)
    }
    if (allowNet && html.isNotEmpty()) markFetched(cache, article.id)
    val shown = html.ifEmpty { usableBody(cached) }
    val thumb = loadThumb(cache, article.id)
        ?: firstDataThumb(shown)?.let(::shrinkThumbDataUrl)?.also { persistThumb(cache, article.id, it) }
    HydratedArticle(shown, thumb)
}

suspend fun prefetchUnread(
    articles: List<Article>,
    readIds: Set<String>,
    cache: EncryptedCache,
    allowNet: Boolean,
    onProgress: suspend (CacheProgress) -> Unit,
) = withContext(Dispatchers.IO) {
    val queue = articles.filter { needsHydrate(cache, it, readIds) }
    articles.forEach { article ->
        val thumb = loadThumb(cache, article.id) ?: return@forEach
        onProgress(CacheProgress(0, queue.size, article.id, thumb))
    }
    var done = 0
    onProgress(CacheProgress(0, queue.size))
    queue.forEach { article ->
        yield()
        val thumb = runCatching { hydrateShared(article, cache, allowNet).thumb }.getOrNull()
            ?: loadThumb(cache, article.id)
        done += 1
        onProgress(CacheProgress(done, queue.size, article.id, thumb))
    }
}

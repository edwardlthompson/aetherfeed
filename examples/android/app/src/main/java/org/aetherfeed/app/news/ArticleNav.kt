package org.aetherfeed.app.news

import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.applock.SessionPlainCache
import org.aetherfeed.app.domain.Article

fun sortArticles(articles: List<Article>, oldestFirst: Boolean): List<Article> {
    val ordered = articles.sortedWith(
        compareBy<Article> { it.publishedAt ?: Long.MAX_VALUE }.thenBy { it.id },
    )
    return if (oldestFirst) ordered else ordered.asReversed()
}

fun neighborArticle(articles: List<Article>, currentId: String?, delta: Int): Article? {
    if (currentId.isNullOrBlank() || articles.isEmpty() || delta == 0) return null
    val index = articles.indexOfFirst { it.id == currentId }
    if (index < 0) return null
    return articles.getOrNull(index + delta)
}

fun prefetchAfter(articles: List<Article>, startId: String?, readIds: Set<String>): List<Article> {
    val start = startId?.let { id -> articles.indexOfFirst { it.id == id } } ?: -1
    if (start < 0) return articles.filter { it.id !in readIds }
    return listOf(articles[start]) + articles.drop(start + 1).filter { it.id !in readIds }
}

fun peekCachedHtml(cache: EncryptedCache, articleId: String): String? {
    val raw = runCatching { cache.read("articles", articleId)?.toString(Charsets.UTF_8) }.getOrNull()
    return usableBody(raw).takeIf { it.isNotEmpty() }
}

fun overlayReaderHtml(
    article: Article,
    selected: Article?,
    bodies: Map<String, String> = emptyMap(),
): String {
    val html = when {
        selected?.id == article.id && !selected.contentHtml.isNullOrEmpty() -> selected.contentHtml
        else -> bodies[article.id] ?: SessionPlainCache.utf8("articles", article.id) ?: article.contentHtml
    }
    return usableBody(html)
}

fun aroundArticles(articles: List<Article>, currentId: String?): List<Article> {
    val current = articles.firstOrNull { it.id == currentId } ?: return emptyList()
    return listOfNotNull(
        neighborArticle(articles, currentId, -1),
        current,
        neighborArticle(articles, currentId, 1),
    )
}

fun peekAround(
    cache: EncryptedCache,
    articles: List<Article>,
    currentId: String?,
): Map<String, String> {
    val out = LinkedHashMap<String, String>()
    for (row in aroundArticles(articles, currentId)) {
        peekCachedHtml(cache, row.id)?.let { out[row.id] = it }
    }
    return out
}

fun peekNeighbors(
    cache: EncryptedCache,
    articles: List<Article>,
    currentId: String?,
    already: Set<String> = SessionPlainCache.ids("articles"),
): Map<String, String> = peekAround(cache, articles, currentId).filterKeys { it != currentId && it !in already }

fun warmFeedEnds(cache: EncryptedCache, articles: List<Article>) {
    listOfNotNull(
        articles.firstOrNull(),
        articles.getOrNull(1),
        articles.getOrNull(articles.lastIndex - 1),
        articles.lastOrNull(),
    ).distinct().forEach { peekCachedHtml(cache, it.id) }
}

fun unreadAmong(ids: Iterable<String>, readIds: Set<String>): Int = ids.count { it !in readIds }

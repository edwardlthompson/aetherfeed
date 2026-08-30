package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Article

const val BODY_FLOOR = 400
private const val MAX_PAGES = 12

private val PAYWALL = listOf("subscriber-only", "subscribe to continue", "paywall")

fun needsFetch(html: String?): Boolean {
    val body = html ?: return true
    if (body.length >= BODY_FLOOR) return false
    return body.trim().length < BODY_FLOOR
}

fun looksPaywalled(html: String?): Boolean {
    val text = html ?: ""
    if (text.isBlank()) return true
    if (text.length >= BODY_FLOOR * 2) return false
    val lower = text.lowercase()
    return PAYWALL.any { it in lower }
}

fun isStubBody(html: String?): Boolean {
    val raw = html.orEmpty()
    if (raw.isBlank() || raw.length >= BODY_FLOOR * 8) return false
    val text = raw.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim()
    if (text.isBlank()) return false
    val lower = text.lowercase()
    val hnMeta = listOf("comments url", "article url", "# comments").any { it in lower }
    return (hnMeta || Regex("https?://").containsMatchIn(text)) && text.length < BODY_FLOOR
}

fun isFeedExcerpt(html: String?): Boolean {
    val raw = html ?: return false
    if (raw.isBlank()) return false
    return raw.contains("webfeedsfeaturedvisual", ignoreCase = true) ||
        raw.contains("link_thumbnail", ignoreCase = true)
}

fun looksIncompletePages(html: String?): Boolean {
    val lower = html.orEmpty().lowercase()
    return "next page" in lower || Regex("""rel=["']next["']""").containsMatchIn(lower)
}

fun usableBody(html: String?): String {
    if (html.isNullOrBlank() || needsFetch(html) || looksPaywalled(html) || isStubBody(html) || isFeedExcerpt(html)) {
        return ""
    }
    return html
}

fun pickReadableRoot(html: String): String {
    val entries = Regex(
        "(?is)<section\\b[^>]*(?:class|id)=['\"][^'\"]*entry-content[^'\"]*['\"][^>]*>(.*?)</section>",
    ).findAll(html).map { it.groupValues[1] }.filter { it.length >= BODY_FLOOR }.toList()
    if (entries.isNotEmpty()) return entries.maxBy { it.length }
    val blocks = ArrayList<String>()
    Regex("(?is)<article\\b[^>]*>(.*?)</article>").findAll(html).forEach { match ->
        if (!match.value.contains("post-pagination", ignoreCase = true)) blocks.add(match.groupValues[1])
    }
    Regex("(?is)<main\\b[^>]*>(.*?)</main>").findAll(html).forEach { blocks.add(it.groupValues[1]) }
    return blocks.maxByOrNull { it.length } ?: html
}

fun nextPageUrl(html: String): String? {
    val patterns = listOf(
        """(?is)<link\b[^>]*rel=["']next["'][^>]*href=["']([^"']+)["']""",
        """(?is)<link\b[^>]*href=["']([^"']+)["'][^>]*rel=["']next["']""",
        """(?is)<a\b[^>]*rel=["']next["'][^>]*href=["']([^"']+)["']""",
        """(?is)<a\b[^>]*href=["']([^"']+)["'][^>]*rel=["']next["']""",
    )
    for (pat in patterns) {
        val href = Regex(pat).find(html)?.groupValues?.getOrNull(1)?.trim().orEmpty()
        if (href.startsWith("http://") || href.startsWith("https://")) return href
    }
    return null
}

fun extractReadable(html: String): String {
    val cleaned = html
        .replace(Regex("(?is)<script.*?</script>"), "")
        .replace(Regex("(?is)<style.*?</style>"), "")
    val raw = promoteLazyImages(pickReadableRoot(cleaned))
    return dropNonContentImages(sanitizeReaderHtml(raw))
}

suspend fun resolveArticleHtml(
    article: Article,
    fetcher: FeedBodyFetcher,
    cachedHtml: String? = null,
): String {
    val cached = usableBody(cachedHtml ?: article.contentHtml)
    if (cached.isNotEmpty()) return cached
    if (article.url.isBlank()) return cached
    val first = runCatching { fetcher.fetch(article.url, 15_000L) }.getOrNull() ?: return cached
    val parts = ArrayList<String>()
    val seen = HashSet<String>()
    var page: String? = first
    var hops = 0
    while (page != null && hops < MAX_PAGES) {
        hops += 1
        parts.add(extractReadable(page))
        val next = nextPageUrl(page) ?: break
        if (!seen.add(next)) break
        page = runCatching { fetcher.fetch(next, 15_000L) }.getOrNull()
    }
    return usableBody(parts.joinToString("")).ifEmpty { cached }
}

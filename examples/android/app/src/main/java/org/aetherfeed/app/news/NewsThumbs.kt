package org.aetherfeed.app.news

import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.domain.Article

const val THUMB_KIND = "thumbs"

fun loadThumb(cache: EncryptedCache, articleId: String): String? =
    runCatching { cache.read(THUMB_KIND, articleId)?.toString(Charsets.UTF_8) }.getOrNull()?.takeIf { it.isNotBlank() }

fun loadThumbs(cache: EncryptedCache, ids: Iterable<String>): Map<String, String> {
    val out = LinkedHashMap<String, String>()
    for (id in ids) {
        if (id.isBlank()) continue
        val thumb = loadThumb(cache, id) ?: continue
        out[id] = thumb
    }
    return out
}

fun persistThumb(cache: EncryptedCache, articleId: String, thumb: String) {
    if (thumb.isBlank() || cache.exists(THUMB_KIND, articleId)) return
    runCatching { cache.write(THUMB_KIND, articleId, thumb.toByteArray()) }
}

fun loadHeadlineThumbs(cache: EncryptedCache, extra: List<Article> = emptyList()): Map<String, String> =
    loadThumbs(cache, extra.map { it.id })

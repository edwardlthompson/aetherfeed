package org.aetherfeed.app.news

import org.aetherfeed.app.applock.EncryptedCache

fun interface ImageBytesFetcher {
    suspend fun fetch(url: String, timeoutMs: Long): ByteArray?
}

suspend fun cacheArticleImages(
    html: String,
    fetcher: ImageBytesFetcher,
    cache: EncryptedCache,
    articleId: String,
    onProgress: (Int, Int) -> Unit = { _, _ -> },
): String {
    val cleaned = dropNonContentImages(html)
    val srcs = collectImageSrcs(cleaned)
    if (srcs.isEmpty()) {
        onProgress(1, 1)
        return cleaned
    }
    val blobs = linkedMapOf<String, String>()
    srcs.forEachIndexed { index, src ->
        onProgress(index, srcs.size)
        val key = "${articleId}-${src.hashCode()}"
        val cached = cache.read("images", key)
        val bytes = cached ?: runCatching { fetcher.fetch(src, IMAGE_TIMEOUT_MS) }.getOrNull()
        if (bytes == null || bytes.isEmpty() || bytes.size > IMAGE_MAX_BYTES) return@forEachIndexed
        if (cached == null) runCatching { cache.write("images", key, bytes) }
        blobs[src] = bytesToDataUrl(bytes, "image/*")
    }
    onProgress(srcs.size, srcs.size)
    return rewriteImageSrcs(cleaned, blobs)
}

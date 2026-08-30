package org.aetherfeed.app.news

import org.aetherfeed.app.applock.EncryptedCache

fun stampCachedAt(cache: EncryptedCache, id: String, now: Long = System.currentTimeMillis()) {
    if (id.isBlank() || cache.exists(CACHED_AT_KIND, id)) return
    runCatching { cache.write(CACHED_AT_KIND, id, now.toString().toByteArray()) }
}

fun readCachedAt(cache: EncryptedCache, id: String): Long? =
    runCatching { cache.read(CACHED_AT_KIND, id)?.toString(Charsets.UTF_8)?.toLongOrNull() }.getOrNull()

fun dropArticleCache(cache: EncryptedCache, id: String) {
    cache.delete("articles", id)
    cache.delete("thumbs", id)
    cache.delete(READY_KIND, id)
    cache.delete(CACHED_AT_KIND, id)
}

fun sweepArticleCache(
    cache: EncryptedCache,
    starred: Set<String>,
    mode: CacheRetainMode,
    now: Long,
    sweepTrigger: Boolean,
    skip: Set<String> = emptySet(),
): Int {
    var gone = 0
    for (id in cache.ids("articles")) {
        if (id in skip) continue
        if (shouldDropCache(readCachedAt(cache, id), id in starred, mode, now, sweepTrigger)) {
            dropArticleCache(cache, id)
            gone += 1
        }
    }
    return gone
}

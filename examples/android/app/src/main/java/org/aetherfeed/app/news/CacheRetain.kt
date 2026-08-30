package org.aetherfeed.app.news

const val CACHE_RETAIN_DAYS = 30L
const val CACHE_RETAIN_MS = CACHE_RETAIN_DAYS * 86_400_000L
const val CACHED_AT_KIND = "cached-at"

enum class CacheRetainMode { Days30, NextSync }

fun shouldDropCache(
    cachedAt: Long?,
    starred: Boolean,
    mode: CacheRetainMode,
    now: Long,
    sweepTrigger: Boolean,
): Boolean {
    if (starred || cachedAt == null) return false
    return when (mode) {
        CacheRetainMode.Days30 -> now - cachedAt >= CACHE_RETAIN_MS
        CacheRetainMode.NextSync -> sweepTrigger
    }
}

fun parseCacheRetainMode(raw: String?): CacheRetainMode =
    if (raw?.trim().equals("sync", ignoreCase = true)) CacheRetainMode.NextSync else CacheRetainMode.Days30

fun encodeCacheRetainMode(mode: CacheRetainMode): String =
    if (mode == CacheRetainMode.NextSync) "sync" else "days30"

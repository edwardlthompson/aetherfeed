package org.aetherfeed.shared.domain

/**
 * News refresh contract for Ktor (12s timeout, UA `AetherFeed/1`, 12-feed cap).
 * Implementation lands with the shared Ktor client; this is the Sprint 56 scaffold surface.
 */
interface NewsHttpClient {
    suspend fun getText(url: String): String
}

const val NEWS_HTTP_TIMEOUT_MS: Long = 12_000
const val NEWS_HTTP_USER_AGENT: String = "AetherFeed/1"
const val NEWS_HTTP_FEED_CAP: Int = 12

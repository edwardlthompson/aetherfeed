package org.aetherfeed.app.news

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ModuleKind

/** Fetch subscribed news feeds over HTTP and ingest into the vault. */
object NewsFeedRefresh {
    suspend fun refresh(library: LibraryRepository, limitFeeds: Int = 12) =
        withContext(Dispatchers.IO) {
            val feeds =
                library.feeds()
                    .filter { it.kind == ModuleKind.News }
                    .sortedByDescending { it.updatedAt }
                    .take(limitFeeds)
            for (feed in feeds) {
                val body = fetch(feed.url) ?: continue
                runCatching {
                    val doc = parseFeed(body, "application/xml", feed.url)
                    DefaultNewsRepository(library).ingest(doc)
                }
            }
        }

    private fun fetch(url: String): ByteArray? {
        val conn =
            (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 12_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "AetherFeed/1")
            }
        return try {
            if (conn.responseCode !in 200..299) {
                null
            } else {
                conn.inputStream.use { it.readBytes() }
            }
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }
}

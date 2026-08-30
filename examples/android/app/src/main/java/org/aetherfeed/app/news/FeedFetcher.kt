package org.aetherfeed.app.news

import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

const val DEFAULT_FEED_TIMEOUT_MS = 10_000L

fun interface FeedBodyFetcher {
    suspend fun fetch(url: String, timeoutMs: Long): String
}

class HttpFeedFetcher : FeedBodyFetcher {
    override suspend fun fetch(url: String, timeoutMs: Long): String = withContext(Dispatchers.IO) {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "AetherFeed/0.1")
            connectTimeout = timeoutMs.toInt().coerceAtLeast(1)
            readTimeout = timeoutMs.toInt().coerceAtLeast(1)
            instanceFollowRedirects = true
        }
        try {
            coroutineContext.ensureActive()
            val code = conn.responseCode
            if (code !in 200..299) throw httpRefreshError(code)
            conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (error: CancellationException) {
            throw NewsRefreshError.Aborted(error.message ?: "Refresh aborted")
        } catch (error: SocketTimeoutException) {
            throw NewsRefreshError.Timeout(error.message ?: "Refresh timed out")
        } finally {
            conn.disconnect()
        }
    }
}

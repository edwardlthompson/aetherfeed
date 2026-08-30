package org.aetherfeed.app.downloads

import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun interface EpisodeFetcher {
    suspend fun fetch(url: String): ByteArray
}

class EpisodeFetchException(message: String, cause: Throwable? = null) : Exception(message, cause)

class HttpEpisodeFetcher(
    private val timeoutMs: Int = DEFAULT_FETCH_TIMEOUT_MS,
) : EpisodeFetcher {
    override suspend fun fetch(url: String): ByteArray = withContext(Dispatchers.IO) {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = timeoutMs.coerceAtLeast(1)
            readTimeout = timeoutMs.coerceAtLeast(1)
            instanceFollowRedirects = true
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) throw EpisodeFetchException("HTTP $code")
            val bytes = conn.inputStream.use { it.readBytes() }
            if (bytes.isEmpty()) throw EpisodeFetchException("empty body")
            bytes
        } catch (error: CancellationException) {
            throw error
        } catch (error: EpisodeFetchException) {
            throw error
        } catch (error: SocketTimeoutException) {
            throw EpisodeFetchException(error.message ?: "timed out", error)
        } catch (error: Exception) {
            throw EpisodeFetchException(error.message ?: "fetch failed", error)
        } finally {
            conn.disconnect()
        }
    }
}

const val DEFAULT_FETCH_TIMEOUT_MS = 30_000

package org.aetherfeed.app.news

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HttpImageFetcher : ImageBytesFetcher {
    override suspend fun fetch(url: String, timeoutMs: Long): ByteArray? = withContext(Dispatchers.IO) {
        if (!isFetchableImageSrc(url)) return@withContext null
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "AetherFeed/0.1")
            connectTimeout = timeoutMs.toInt().coerceAtLeast(1)
            readTimeout = timeoutMs.toInt().coerceAtLeast(1)
            instanceFollowRedirects = true
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) return@withContext null
            val bytes = conn.inputStream.use { it.readBytes() }
            if (bytes.isEmpty() || bytes.size > IMAGE_MAX_BYTES) null else bytes
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }
}

package org.aetherfeed.app.booru

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

interface BooruHttpClient {
    suspend fun getBytes(url: String): ByteArray
}

class UrlConnectionBooruHttpClient : BooruHttpClient {
    override suspend fun getBytes(url: String): ByteArray =
        withContext(Dispatchers.IO) {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            conn.requestMethod = "GET"
            try {
                val code = conn.responseCode
                check(code in 200..299) { "HTTP $code for $url" }
                conn.inputStream.use { it.readBytes() }
            } finally {
                conn.disconnect()
            }
        }
}

/** Test double — returns a fixed body per exact URL match. */
class MapBooruHttpClient(
    private val bodies: Map<String, ByteArray>,
) : BooruHttpClient {
    override suspend fun getBytes(url: String): ByteArray =
        bodies[url] ?: error("No fixture for url: $url")
}

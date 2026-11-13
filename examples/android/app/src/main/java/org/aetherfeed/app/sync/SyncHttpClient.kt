package org.aetherfeed.app.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class SyncHttpResponse(
    val status: Int,
    val body: ByteArray,
    val headers: Map<String, String> = emptyMap(),
)

interface SyncHttpClient {
    suspend fun request(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: ByteArray? = null,
    ): SyncHttpResponse
}

class UrlConnectionSyncHttpClient : SyncHttpClient {
    override suspend fun request(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: ByteArray?,
    ): SyncHttpResponse =
        withContext(Dispatchers.IO) {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 20_000
            conn.readTimeout = 60_000
            conn.requestMethod = method.uppercase()
            conn.doInput = true
            if (body != null) {
                conn.doOutput = true
            }
            headers.forEach { (key, value) -> conn.setRequestProperty(key, value) }
            try {
                if (body != null) {
                    conn.outputStream.use { it.write(body) }
                }
                val status = conn.responseCode
                val stream = if (status in 200..299) conn.inputStream else conn.errorStream
                val bytes = stream?.use { it.readBytes() } ?: ByteArray(0)
                SyncHttpResponse(status, bytes)
            } finally {
                conn.disconnect()
            }
        }
}

/** Test double — dispatches by method + URL to a handler. */
class RecordingSyncHttpClient(
    private val handler: suspend (method: String, url: String, headers: Map<String, String>, body: ByteArray?) -> SyncHttpResponse,
) : SyncHttpClient {
    val requests = mutableListOf<Quad>()

    data class Quad(
        val method: String,
        val url: String,
        val headers: Map<String, String>,
        val body: ByteArray?,
    )

    override suspend fun request(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: ByteArray?,
    ): SyncHttpResponse {
        requests.add(Quad(method, url, headers, body?.copyOf()))
        return handler(method, url, headers, body)
    }
}

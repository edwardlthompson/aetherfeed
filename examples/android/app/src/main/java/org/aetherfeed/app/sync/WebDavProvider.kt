package org.aetherfeed.app.sync

import org.aetherfeed.app.domain.SyncProvider
import java.util.Base64

/** Self-hosted WebDAV fallback. Uploads opaque `.enc` blobs only. */
class WebDavProvider(
    private val http: SyncHttpClient,
    private val endpoint: String,
    private val username: String? = null,
    private val password: String? = null,
    private val blobName: String = SYNC_BLOB_NAME,
) : SyncProvider {
    override val id: String = "webdav"

    init {
        requireOpaqueEncName(blobName)
        require(endpoint.isNotBlank()) { "WebDAV endpoint required" }
    }

    override suspend fun pull(): ByteArray? {
        val url = blobUrl()
        val resp = http.request("GET", url, headers = authHeaders(), body = null)
        return when (resp.status) {
            404 -> null
            in 200..299 -> resp.body.takeIf { it.isNotEmpty() }
            else -> error("WebDAV pull HTTP ${resp.status}")
        }
    }

    override suspend fun push(blob: ByteArray) {
        require(blob.isNotEmpty())
        val url = blobUrl()
        val resp =
            http.request(
                "PUT",
                url,
                headers =
                    authHeaders() +
                        mapOf("Content-Type" to "application/octet-stream"),
                body = blob,
            )
        check(resp.status in 200..299) { "WebDAV push HTTP ${resp.status}" }
    }

    private fun blobUrl(): String {
        val base = endpoint.trimEnd('/')
        return "$base/$blobName"
    }

    private fun authHeaders(): Map<String, String> {
        val user = username?.trim().orEmpty()
        val pass = password?.trim().orEmpty()
        if (user.isEmpty() && pass.isEmpty()) return emptyMap()
        val token =
            Base64.getEncoder()
                .encodeToString("$user:$pass".toByteArray(Charsets.UTF_8))
        return mapOf("Authorization" to "Basic $token")
    }
}

package org.aetherfeed.app.sync

import org.aetherfeed.app.domain.SyncProvider
import org.json.JSONObject

/**
 * Google Drive App Data folder only (`drive.appdata` scope). Never writes to My Drive root.
 */
class DriveAppDataProvider(
    private val http: SyncHttpClient,
    private val accessToken: String,
    private val blobName: String = SYNC_BLOB_NAME,
    private val fileIdStore: DriveFileIdStore = InMemoryDriveFileIdStore(),
) : SyncProvider {
    override val id: String = "drive-appdata"

    init {
        requireOpaqueEncName(blobName)
        require(accessToken.isNotBlank())
    }

    override suspend fun pull(): ByteArray? {
        val fileId = fileIdStore.read() ?: findFileId() ?: return null
        val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
        val resp =
            http.request(
                "GET",
                url,
                headers = authHeaders(),
                body = null,
            )
        check(resp.status in 200..299) { "Drive pull HTTP ${resp.status}" }
        return resp.body.takeIf { it.isNotEmpty() }
    }

    override suspend fun push(blob: ByteArray) {
        require(blob.isNotEmpty())
        val existingId = fileIdStore.read() ?: findFileId()
        if (existingId == null) {
            create(blob)
        } else {
            update(existingId, blob)
        }
    }

    private suspend fun findFileId(): String? {
        val query = "name = '$blobName'"
        val url =
            "https://www.googleapis.com/drive/v3/files" +
                "?spaces=appDataFolder&fields=files(id,name)&q=${java.net.URLEncoder.encode(query, "UTF-8")}"
        val resp = http.request("GET", url, headers = authHeaders(), body = null)
        if (resp.status == 404) return null
        check(resp.status in 200..299) { "Drive list HTTP ${resp.status}" }
        val files = JSONObject(resp.body.toString(Charsets.UTF_8)).optJSONArray("files") ?: return null
        if (files.length() == 0) return null
        val id = files.getJSONObject(0).optString("id", "").trim()
        if (id.isNotEmpty()) {
            fileIdStore.write(id)
        }
        return id.takeIf { it.isNotEmpty() }
    }

    private suspend fun create(blob: ByteArray) {
        val url =
            "https://www.googleapis.com/upload/drive/v3/files?uploadType=media" +
                "&fields=id&spaces=appDataFolder"
        val resp =
            http.request(
                "POST",
                url,
                headers =
                    authHeaders() +
                        mapOf(
                            "Content-Type" to "application/octet-stream",
                            "X-Upload-Content-Type" to "application/octet-stream",
                        ),
                body = blob,
            )
        check(resp.status in 200..299) { "Drive create HTTP ${resp.status}" }
        val id = JSONObject(resp.body.toString(Charsets.UTF_8)).optString("id", "").trim()
        check(id.isNotEmpty()) { "Drive create missing file id" }
        patchMetadata(id)
        fileIdStore.write(id)
    }

    private suspend fun update(fileId: String, blob: ByteArray) {
        val url =
            "https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media"
        val resp =
            http.request(
                "PATCH",
                url,
                headers =
                    authHeaders() +
                        mapOf(
                            "Content-Type" to "application/octet-stream",
                        ),
                body = blob,
            )
        check(resp.status in 200..299) { "Drive update HTTP ${resp.status}" }
        fileIdStore.write(fileId)
    }

    private suspend fun patchMetadata(fileId: String) {
        val url = "https://www.googleapis.com/drive/v3/files/$fileId?fields=id"
        val json =
            JSONObject()
                .put("name", blobName)
                .put("parents", org.json.JSONArray().put("appDataFolder"))
                .toString()
        val resp =
            http.request(
                "PATCH",
                url,
                headers = authHeaders() + mapOf("Content-Type" to "application/json"),
                body = json.toByteArray(Charsets.UTF_8),
            )
        check(resp.status in 200..299) { "Drive metadata HTTP ${resp.status}" }
    }

    private fun authHeaders(): Map<String, String> =
        mapOf("Authorization" to "Bearer $accessToken")
}

interface DriveFileIdStore {
    fun read(): String?
    fun write(id: String)
}

class InMemoryDriveFileIdStore : DriveFileIdStore {
    private var fileId: String? = null

    override fun read(): String? = fileId

    override fun write(id: String) {
        fileId = id
    }
}

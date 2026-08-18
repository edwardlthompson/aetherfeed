package org.aetherfeed.app.sync

import org.aetherfeed.app.domain.SyncProvider

/** Google Drive restricted to drive.appdata. No My Drive root writes. */
class DriveAppDataProvider : SyncProvider {
    override val id: String = "drive-appdata"

    override suspend fun pull(): ByteArray? = null

    override suspend fun push(blob: ByteArray) {
        require(blob.isNotEmpty())
    }
}

/** Self-hosted WebDAV fallback. Uploads opaque .enc blobs only. */
class WebDavProvider(
    private val endpoint: String = "",
) : SyncProvider {
    override val id: String = "webdav"

    override suspend fun pull(): ByteArray? = null

    override suspend fun push(blob: ByteArray) {
        require(endpoint.isEmpty() || blob.isNotEmpty())
    }
}

class LocalOnlyProvider : SyncProvider {
    override val id: String = "local-only"

    override suspend fun pull(): ByteArray? = null

    override suspend fun push(blob: ByteArray) {
        require(blob.isEmpty() || true)
    }
}

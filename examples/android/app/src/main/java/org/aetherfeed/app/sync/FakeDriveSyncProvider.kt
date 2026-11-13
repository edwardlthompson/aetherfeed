package org.aetherfeed.app.sync

import org.aetherfeed.app.domain.SyncProvider
import java.io.File

/**
 * Local fake for unit tests and offline dev — mirrors Drive appdata semantics without HTTP.
 */
class FakeDriveSyncProvider(
    private val blobName: String = SYNC_BLOB_NAME,
    private val directory: File? = null,
) : SyncProvider {
    override val id: String = "drive-appdata"

    private val memory = mutableMapOf<String, ByteArray>()

    init {
        requireOpaqueEncName(blobName)
    }

    override suspend fun pull(): ByteArray? {
        directory?.let { dir ->
            val file = File(dir, blobName)
            if (!file.isFile) return null
            return file.readBytes()
        }
        return memory[blobName]?.copyOf()
    }

    override suspend fun push(blob: ByteArray) {
        require(blob.isNotEmpty())
        directory?.let { dir ->
            dir.mkdirs()
            File(dir, blobName).writeBytes(blob)
            return
        }
        memory[blobName] = blob.copyOf()
    }
}

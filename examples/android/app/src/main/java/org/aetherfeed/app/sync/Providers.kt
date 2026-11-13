package org.aetherfeed.app.sync

import org.aetherfeed.app.domain.SyncProvider

class LocalOnlyProvider : SyncProvider {
    override val id: String = "local-only"

    override suspend fun pull(): ByteArray? = null

    override suspend fun push(blob: ByteArray) {
        require(blob.isNotEmpty()) { "local-only mode never uploads" }
        error("local-only: sync uploads disabled")
    }
}

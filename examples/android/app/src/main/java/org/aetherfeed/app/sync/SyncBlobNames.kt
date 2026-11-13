package org.aetherfeed.app.sync

internal const val SYNC_BLOB_NAME = "aetherfeed-sync.enc"

internal fun requireOpaqueEncName(name: String) {
    require(name.endsWith(".enc") && !name.contains("..")) { "sync blob must be opaque .enc" }
}

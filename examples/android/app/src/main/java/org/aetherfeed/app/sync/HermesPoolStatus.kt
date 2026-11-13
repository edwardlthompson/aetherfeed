package org.aetherfeed.app.sync

data class HermesPoolStatus(
    val message: String = "Hermes not checked",
    val lastSyncAt: Long = 0L,
    val ok: Boolean = true,
)

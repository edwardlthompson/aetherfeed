package org.aetherfeed.app.domain

data class Tag(
    val id: String,
    val name: String,
)

data class Star(
    val targetId: String,
    val module: ModuleKind,
    val createdAt: Long,
)

data class Like(
    val targetId: String,
    val module: ModuleKind,
    val createdAt: Long,
)

data class ReadState(
    val targetId: String,
    val module: ModuleKind,
    val status: ReadStatus,
    val updatedAt: Long,
)

data class PlaybackPosition(
    val episodeId: String,
    val positionMs: Long,
    val durationMs: Long? = null,
    val updatedAt: Long,
)

data class NotificationChannelPref(
    val id: String,
    val module: ModuleKind,
    val enabled: Boolean,
    val label: String,
)

data class SyncEnvelope(
    val version: Int,
    val kdf: String,
    val aead: String,
    val saltB64: String,
    val nonceB64: String,
    val ciphertextB64: String,
) {
    init {
        require(version == 1)
        require(kdf == "argon2id")
        require(aead == "xchacha20poly1305" || aead == "aes-256-gcm")
    }
}

fun totalUnread(states: Collection<ReadState>): Int =
    states.count { it.status != ReadStatus.Read }

fun unreadByModule(states: Collection<ReadState>): Map<ModuleKind, Int> =
    ModuleKind.entries.associateWith { kind ->
        states.count { it.module == kind && it.status != ReadStatus.Read }
    }

package org.aetherfeed.app.sync

/** Keep in sync with Hermes sibling `org.hermeslauncher.app.feeds.pool` pool models. */
data class PoolSub(
    val url: String,
    val title: String = "",
    val kind: String,
    val tag: String = "",
    val updatedAt: Long = 0L,
)

data class PoolRead(
    val url: String,
    val read: Boolean = true,
    val updatedAt: Long = 0L,
)

data class PoolStar(
    val url: String,
    val starred: Boolean = true,
    val updatedAt: Long = 0L,
)

data class PoolPlayback(
    val url: String,
    val positionMs: Long = 0L,
    val updatedAt: Long = 0L,
)

data class PoolDocument(
    val version: Int = 2,
    val updatedAt: Long = 0L,
    val subs: List<PoolSub> = emptyList(),
    val reads: List<PoolRead> = emptyList(),
    val stars: List<PoolStar> = emptyList(),
    val playback: List<PoolPlayback> = emptyList(),
)

sealed class PoolCodecError(message: String) : Exception(message) {
    class InvalidJson(message: String) : PoolCodecError(message)

    class InvalidKind(message: String) : PoolCodecError(message)

    class BlankUrl : PoolCodecError("blank url")

    class UnsupportedVersion(version: Int) : PoolCodecError("unsupported version $version")
}

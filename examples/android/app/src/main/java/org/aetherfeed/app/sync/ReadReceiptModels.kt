package org.aetherfeed.app.sync

/** Keep in sync with Hermes `org.hermeslauncher.app.feeds.pool` read receipt models. */
data class ReadReceipt(
    val url: String,
    val read: Boolean = true,
    val updatedAt: Long = 0L,
)

data class ReadReceiptDocument(
    val version: Int = 1,
    val updatedAt: Long = 0L,
    val reads: List<ReadReceipt> = emptyList(),
)

sealed class ReadReceiptError(message: String) : Exception(message) {
    class InvalidJson(message: String) : ReadReceiptError(message)

    class BlankUrl : ReadReceiptError("blank url")

    class UnsupportedVersion(version: Int) : ReadReceiptError("unsupported version $version")
}

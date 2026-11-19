package org.aetherfeed.shared.domain.sync

/** LAN pool client settings — same fields as Settings (TECH_DECISION §7). */
data class LanPoolSettings(
    val hubUrl: String = "",
    val passphrase: String = "",
) {
    fun ready(): Boolean = hubUrl.isNotBlank() && passphrase.isNotBlank()
}
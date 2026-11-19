package org.aetherfeed.shared.domain.sync

/**
 * SyncEnvelope wire shape (ADR-0011). Codec bodies move from Android/TS/Rust into shared Kotlin
 * during Sprint 56–61; this scaffold locks the package and version field.
 */
data class SyncEnvelope(
    val version: Int = 1,
    val ciphertextBase64: String,
    val nonceBase64: String,
    val saltBase64: String,
)

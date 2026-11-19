package org.aetherfeed.desktop.vault

import org.aetherfeed.shared.vault.InMemoryVaultKeychain
import org.aetherfeed.shared.vault.VaultKeychain

/**
 * Desktop OS credential store boundary.
 *
 * TODO(Sprint 59+): Windows — Credential Manager (`CredWrite` / `CredRead`).
 * TODO: macOS Keychain, Linux libsecret — keep aliases identical to Android vault.
 */
class DesktopVaultKeychain : VaultKeychain {
    private val memory = InMemoryVaultKeychain()

    override fun storeSecret(alias: String, value: ByteArray) {
        // Replace with platform store when row 2+ lands; memory keeps dev builds working.
        memory.storeSecret(alias, value)
    }

    override fun loadSecret(alias: String): ByteArray? = memory.loadSecret(alias)

    override fun deleteSecret(alias: String) {
        memory.deleteSecret(alias)
    }
}
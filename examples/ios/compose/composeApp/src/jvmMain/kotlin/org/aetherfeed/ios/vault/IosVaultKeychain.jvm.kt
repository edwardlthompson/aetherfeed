package org.aetherfeed.ios.vault

import org.aetherfeed.shared.vault.InMemoryVaultKeychain
import org.aetherfeed.shared.vault.VaultKeychain

actual fun createIosVaultKeychain(): VaultKeychain = JvmIosVaultKeychain()

/** No Darwin Keychain on JVM — mirrors TECH_DECISION until `iosMain` SecItem wiring lands. */
class JvmIosVaultKeychain : VaultKeychain by InMemoryVaultKeychain()
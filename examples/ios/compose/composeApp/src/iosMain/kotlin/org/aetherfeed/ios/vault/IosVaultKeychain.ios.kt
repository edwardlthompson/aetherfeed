package org.aetherfeed.ios.vault

import org.aetherfeed.shared.vault.InMemoryVaultKeychain
import org.aetherfeed.shared.vault.VaultKeychain

/**
 * TODO(mac): replace with Keychain Services (`kSecClassGenericPassword`, `kSecAttrAccessibleAfterFirstUnlock`).
 * Keep alias strings identical to Android vault (`vault_passphrase`, LAN secret aliases).
 */
actual fun createIosVaultKeychain(): VaultKeychain = InMemoryVaultKeychain()
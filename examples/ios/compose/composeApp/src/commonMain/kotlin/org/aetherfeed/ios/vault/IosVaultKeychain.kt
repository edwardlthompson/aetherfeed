package org.aetherfeed.ios.vault

import org.aetherfeed.shared.vault.VaultKeychain

/**
 * iOS Keychain boundary (decision 9). Darwin `SecItem*` actuals belong in `iosMain` on a Mac;
 * JVM stub uses in-memory storage for Windows/Linux compile and simulator-only workflow docs.
 */
expect fun createIosVaultKeychain(): VaultKeychain
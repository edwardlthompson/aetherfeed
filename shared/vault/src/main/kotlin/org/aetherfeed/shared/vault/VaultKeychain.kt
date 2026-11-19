package org.aetherfeed.shared.vault

/**
 * OS keychain / keystore boundary (TECH_DECISION §4). Platform actuals replace this JVM stub
 * in later Sprint 56–59 work; Android continues EncryptedSharedPreferences until cutover.
 */
interface VaultKeychain {
    fun storeSecret(alias: String, value: ByteArray)

    fun loadSecret(alias: String): ByteArray?

    fun deleteSecret(alias: String)
}

/** In-memory stub for JVM unit tests and early desktop bring-up. */
class InMemoryVaultKeychain : VaultKeychain {
    private val secrets = mutableMapOf<String, ByteArray>()

    override fun storeSecret(alias: String, value: ByteArray) {
        secrets[alias] = value.copyOf()
    }

    override fun loadSecret(alias: String): ByteArray? = secrets[alias]?.copyOf()

    override fun deleteSecret(alias: String) {
        secrets.remove(alias)
    }
}

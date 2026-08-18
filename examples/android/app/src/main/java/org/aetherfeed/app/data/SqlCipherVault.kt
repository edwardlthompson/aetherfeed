package org.aetherfeed.app.data

/**
 * SQLCipher vault entry point. The seed keeps an in-memory library so unit
 * tests stay hermetic; the production open() path lands with Room + SQLCipher.
 */
class SqlCipherVault(
    private val databaseName: String = "aetherfeed-vault.db",
) {
    fun privateDatabaseName(): String = databaseName

    fun isWorldReadable(): Boolean = false
}

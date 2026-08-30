package org.aetherfeed.app.applock

object AppLockHolder {
    @Volatile
    var session: AppLockSession? = null

    fun vaultKey(): ByteArray? = session?.sessionVaultKey()
}

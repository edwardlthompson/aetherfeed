package org.aetherfeed.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SqlCipherVaultTest {
    @Test
    fun staysInAppPrivateStorage() {
        val vault = SqlCipherVault()
        assertEquals("aetherfeed-vault.db", vault.privateDatabaseName())
        assertFalse(vault.isWorldReadable())
    }
}

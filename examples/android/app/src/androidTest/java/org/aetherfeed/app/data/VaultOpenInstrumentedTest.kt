package org.aetherfeed.app.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VaultOpenInstrumentedTest {
    @Test
    fun opensEncryptedVaultOnDevice() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val vault = SqlCipherVault(databaseName = "aetherfeed-vault-instrumented.db")
        val passphrase = ByteArray(32) { 7 }
        try {
            val db = vault.open(context, passphrase)
            assertTrue(vault.isOpen())
            assertTrue(db.isOpen)
            db.query("SELECT 1", null).use { cursor ->
                assertTrue(cursor.moveToFirst())
            }
        } finally {
            vault.close()
            context.deleteDatabase("aetherfeed-vault-instrumented.db")
        }
    }
}

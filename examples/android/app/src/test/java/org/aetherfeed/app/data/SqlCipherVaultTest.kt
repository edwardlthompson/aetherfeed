package org.aetherfeed.app.data

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class SqlCipherVaultTest {
    @Test
    fun staysInAppPrivateStorage() {
        val vault = SqlCipherVault()
        assertEquals("aetherfeed-vault.db", vault.privateDatabaseName())
        assertFalse(vault.isWorldReadable())
    }

    @Test
    fun opensInMemoryVault() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val vault = SqlCipherVault()
        val db = vault.openInMemory(context)
        assertTrue(vault.isOpen())
        db.query("SELECT 1", null).use { cursor ->
            assertTrue(cursor.moveToFirst())
        }
        vault.close()
        assertFalse(vault.isOpen())
    }
}

package org.aetherfeed.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.data.RoomLibraryRepository
import org.aetherfeed.app.data.SqlCipherVault
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VaultOpenInstrumentedTest {
    @Test
    fun opensEncryptedVaultAndPersistsFeed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val vault = SqlCipherVault(databaseName = "aetherfeed-vault-test.db")
        assertFalse(vault.isWorldReadable())
        val db = vault.open(context)
        try {
            val repo = RoomLibraryRepository(db.libraryDao())
            runBlocking {
                repo.upsertFeed(
                    Feed(
                        id = "vault-1",
                        title = "Vault",
                        url = "https://example.invalid/rss.xml",
                        kind = ModuleKind.News,
                        updatedAt = 2,
                    ),
                )
                assertEquals("Vault", repo.feeds().single().title)
            }
            val dbFile = context.getDatabasePath(vault.privateDatabaseName())
            assertTrue(dbFile.exists())
            assertFalse(dbFile.canRead() && dbFile.parentFile?.name == "shared_prefs")
        } finally {
            db.close()
            context.deleteDatabase(vault.privateDatabaseName())
        }
    }
}

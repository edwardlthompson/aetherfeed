package org.aetherfeed.app.data

import android.content.Context
import androidx.room.Room
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.aetherfeed.app.data.local.AetherFeedDatabase
import org.aetherfeed.app.data.local.MIGRATION_1_2

/**
 * Opens the app-private SQLCipher Room database. The passphrase is a
 * device-generated key wrapped by Android Keystore (ADR-0002).
 */
class SqlCipherVault(
    private val databaseName: String = "aetherfeed-vault.db",
) {
    fun privateDatabaseName(): String = databaseName

    fun isWorldReadable(): Boolean = false

    fun open(context: Context): AetherFeedDatabase {
        System.loadLibrary("sqlcipher")
        val factory = SupportOpenHelperFactory(VaultKeyStore(context).passphrase())
        return Room.databaseBuilder(context, AetherFeedDatabase::class.java, databaseName)
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    fun openInMemory(context: Context): AetherFeedDatabase {
        return Room.inMemoryDatabaseBuilder(context, AetherFeedDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}

package org.aetherfeed.app.data

import android.content.Context
import androidx.room.Room
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.aetherfeed.app.data.local.VaultDatabase

/**
 * Opens the Room library behind SQLCipher (ADR-0002).
 * Unit tests may use [openInMemory] without native SQLCipher.
 */
class SqlCipherVault(
    private val databaseName: String = "aetherfeed-vault.db",
) {
    @Volatile
    private var database: VaultDatabase? = null

    fun privateDatabaseName(): String = databaseName

    fun isWorldReadable(): Boolean = false

    fun open(context: Context, passphrase: ByteArray): VaultDatabase {
        database?.let { return it }
        System.loadLibrary("sqlcipher")
        val factory = SupportOpenHelperFactory(passphrase)
        val db =
            Room.databaseBuilder(
                context.applicationContext,
                VaultDatabase::class.java,
                databaseName,
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        database = db
        return db
    }

    fun openInMemory(context: Context): VaultDatabase {
        database?.let { return it }
        val db =
            Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                VaultDatabase::class.java,
            )
                .allowMainThreadQueries()
                .build()
        database = db
        return db
    }

    fun isOpen(): Boolean = database != null

    fun close() {
        database?.close()
        database = null
    }
}

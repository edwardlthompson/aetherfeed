package org.aetherfeed.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.aetherfeed.app.data.SqlCipherVault
import org.aetherfeed.app.data.VaultPassphraseStore
import org.aetherfeed.app.data.local.VaultDatabase
import org.aetherfeed.app.domain.SyncProvider
import org.aetherfeed.app.sync.LocalOnlyProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providePassphraseStore(
        @ApplicationContext context: Context,
    ): VaultPassphraseStore = VaultPassphraseStore(context)

    @Provides
    @Singleton
    fun provideVault(): SqlCipherVault = SqlCipherVault()

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        vault: SqlCipherVault,
        keys: VaultPassphraseStore,
    ): VaultDatabase = vault.open(context, keys.passphraseBytes())

    @Provides
    @Singleton
    fun provideSyncProvider(): SyncProvider = LocalOnlyProvider()
}

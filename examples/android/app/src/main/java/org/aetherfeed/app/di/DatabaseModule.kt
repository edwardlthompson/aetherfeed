package org.aetherfeed.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.aetherfeed.app.data.SqlCipherVault
import org.aetherfeed.app.data.local.AetherFeedDatabase
import org.aetherfeed.app.data.local.LibraryDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AetherFeedDatabase {
        return SqlCipherVault().open(context)
    }

    @Provides
    fun provideLibraryDao(database: AetherFeedDatabase): LibraryDao = database.libraryDao()
}

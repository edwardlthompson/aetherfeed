package org.aetherfeed.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.aetherfeed.app.data.RoomLibraryRepository
import org.aetherfeed.app.domain.LibraryRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: RoomLibraryRepository): LibraryRepository
}

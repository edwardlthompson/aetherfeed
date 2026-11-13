package org.aetherfeed.app.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.aetherfeed.app.domain.LibraryRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotifyEntryPoint {
    fun libraryRepository(): LibraryRepository
}

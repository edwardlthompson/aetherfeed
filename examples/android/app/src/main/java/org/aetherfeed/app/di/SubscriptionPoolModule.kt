package org.aetherfeed.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.news.DefaultNewsRepository
import org.aetherfeed.app.news.ReaderPrefs
import org.aetherfeed.app.news.NewsRepository
import org.aetherfeed.app.sync.FeedChangeNotifier
import org.aetherfeed.app.sync.LanPoolPrefs
import org.aetherfeed.app.sync.LanPoolSync
import org.aetherfeed.app.sync.ReadReceiptSync
import org.aetherfeed.app.sync.SubscriptionPoolClient
import org.aetherfeed.app.sync.SubscriptionPoolSync
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SubscriptionPoolModule {
    @Provides
    @Singleton
    fun provideSubscriptionPoolClient(
        @ApplicationContext context: Context,
    ): SubscriptionPoolClient = SubscriptionPoolClient(context)

    @Provides
    @Singleton
    fun provideSubscriptionPoolSync(
        library: LibraryRepository,
        client: SubscriptionPoolClient,
        feedChangeNotifier: FeedChangeNotifier,
    ): SubscriptionPoolSync = SubscriptionPoolSync(library, client, feedChangeNotifier)

    @Provides
    @Singleton
    fun provideLanPoolPrefs(
        @ApplicationContext context: Context,
    ): LanPoolPrefs = LanPoolPrefs(context)

    @Provides
    @Singleton
    fun provideReaderPrefs(
        @ApplicationContext context: Context,
    ): ReaderPrefs = ReaderPrefs(context)

    @Provides
    @Singleton
    fun provideLanPoolSync(
        library: LibraryRepository,
        prefs: LanPoolPrefs,
        feedChangeNotifier: FeedChangeNotifier,
    ): LanPoolSync = LanPoolSync(library, prefs, feedChangeNotifier)

    @Provides
    @Singleton
    fun provideReadReceiptSync(
        @ApplicationContext context: Context,
        library: LibraryRepository,
    ): ReadReceiptSync = ReadReceiptSync(context, library)

    @Provides
    @Singleton
    fun provideNewsRepository(
        library: LibraryRepository,
        notifier: FeedChangeNotifier,
    ): NewsRepository =
        DefaultNewsRepository(library) {
            notifier.notifyChanged()
        }
}

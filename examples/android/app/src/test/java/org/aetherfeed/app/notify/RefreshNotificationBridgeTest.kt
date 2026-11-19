package org.aetherfeed.app.notify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.PlaybackPosition
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.Star
import org.aetherfeed.app.domain.Tag
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RefreshNotificationBridgeTest {
    private lateinit var context: Context
    private lateinit var poster: ArticleNotificationPoster

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        poster = ArticleNotificationPoster(context)
    }

    @Test
    fun notifyOnUnreadDeltaNoOpsWhenFlat() =
        runBlocking {
            val library = StubLibrary(unread = 2)
            notifyOnUnreadDelta(library, 2, poster)
            assertEquals(2, library.unread)
        }

    @Test
    fun notifyOnUnreadDeltaRunsWhenUnreadGrows() =
        runBlocking {
            val library = StubLibrary(unread = 3)
            notifyOnUnreadDelta(library, 1, poster)
            assertEquals(3, library.unread)
        }

    private class StubLibrary(var unread: Int) : LibraryRepository {
        override suspend fun unreadCount(): Int = unread

        override suspend fun feeds(): List<Feed> = emptyList()

        override suspend fun upsertFeed(feed: Feed) = error("unused")

        override suspend fun deleteFeed(id: String) = error("unused")

        override suspend fun articles(feedId: String?): List<Article> = emptyList()

        override suspend fun upsertArticle(article: Article) = error("unused")

        override suspend fun articleBody(id: String): String? = null

        override suspend fun cacheArticleBody(id: String, body: String) = error("unused")

        override suspend fun readState(targetId: String): ReadState? = null

        override suspend fun readStates(): List<ReadState> = emptyList()

        override suspend fun upsertReadState(state: ReadState) = error("unused")

        override suspend fun stars(): List<Star> = emptyList()

        override suspend fun upsertStar(star: Star) = error("unused")

        override suspend fun removeStar(targetId: String) = error("unused")

        override suspend fun tags(): List<Tag> = emptyList()

        override suspend fun playback(episodeId: String): PlaybackPosition? = null

        override suspend fun playbackAll(): List<PlaybackPosition> = emptyList()

        override suspend fun upsertPlayback(position: PlaybackPosition) = error("unused")
    }
}
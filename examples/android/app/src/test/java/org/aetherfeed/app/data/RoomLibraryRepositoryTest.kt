package org.aetherfeed.app.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class RoomLibraryRepositoryTest {
    @Test
    fun persistsFeedAndUnreadCount() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = SqlCipherVault().openInMemory(context)
        val repo = RoomLibraryRepository(db.libraryDao())
        repo.upsertFeed(
            Feed(
                id = "news-1",
                title = "Local",
                url = "https://example.invalid/feed.xml",
                kind = ModuleKind.News,
                updatedAt = 1,
                folder = "Art",
            ),
        )
        repo.upsertReadState(
            ReadState(
                targetId = "article-1",
                module = ModuleKind.News,
                status = ReadStatus.Unread,
                updatedAt = 1,
            ),
        )
        assertEquals(1, repo.feeds().size)
        assertEquals("Local", repo.feeds().first().title)
        assertEquals("Art", repo.feeds().first().folder)
        assertEquals(1, repo.unreadCount())
        assertTrue(repo.readState("article-1")?.status == ReadStatus.Unread)
        db.close()
    }
}

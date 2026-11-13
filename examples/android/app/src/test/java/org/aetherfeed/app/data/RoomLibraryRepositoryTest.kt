package org.aetherfeed.app.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus
import org.aetherfeed.app.sync.FeedChangeNotifier
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class RoomLibraryRepositoryTest {
    private lateinit var vault: SqlCipherVault
    private lateinit var repo: RoomLibraryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        vault = SqlCipherVault()
        repo = RoomLibraryRepository(vault.openInMemory(context), FeedChangeNotifier())
    }

    @After
    fun tearDown() {
        vault.close()
    }

    @Test
    fun persistsFeedAndUnread() =
        runBlocking {
            repo.upsertFeed(
                Feed(
                    id = "f1",
                    title = "Local",
                    url = "https://example.test/feed",
                    kind = ModuleKind.News,
                    updatedAt = 1L,
                ),
            )
            repo.upsertReadState(
                ReadState(
                    targetId = "a1",
                    module = ModuleKind.News,
                    status = ReadStatus.Unread,
                    updatedAt = 2L,
                ),
            )
            assertEquals(1, repo.feeds().size)
            assertEquals(1, repo.unreadCount())
        }
}

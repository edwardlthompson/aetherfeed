package org.aetherfeed.app.readerimport

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.data.InMemoryLibrary
import org.aetherfeed.app.domain.ModuleKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class SeedLibraryTest {
    @Test
    fun applyHonorsKindFolderAndKeepsSeed() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dest = File(context.filesDir, SEED_LIBRARY_NAME)
        dest.writeText(
            """
            [
              {"id":"n","title":"Local","url":"https://example.invalid/rss.xml","kind":"news","folder":"Art"},
              {"id":"p","title":"Cast","url":"https://feeds.libsyn.com/9/rss","kind":"podcast","folder":"Podcasts"}
            ]
            """.trimIndent(),
        )
        val library = InMemoryLibrary()
        val result = applySeedLibrary(context, library)!!
        assertEquals(2, result.feedsAdded)
        assertEquals(1, result.newsAdded)
        assertEquals(1, result.podcastsAdded)
        assertEquals("Art", library.feeds().first { it.kind == ModuleKind.News }.folder)
        assertTrue(dest.exists())
        assertEquals(0, applySeedLibrary(context, library)!!.feedsAdded)
        assertEquals(2, library.feeds().size)
    }

    @Test
    fun seedMergesPastSmokeAndDropsSmokeOnly() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val library = InMemoryLibrary()
        library.upsertFeed(
            org.aetherfeed.app.domain.Feed(
                "smoke",
                "HN",
                "https://hnrss.org/frontpage",
                ModuleKind.News,
                updatedAt = 1,
                folder = "Smoke",
            ),
        )
        File(context.filesDir, SEED_LIBRARY_NAME).writeText(
            """[{"title":"Local","url":"https://example.invalid/rss.xml","kind":"news","folder":"Art"}]""",
        )
        applySeedLibrary(context, library)
        val folders = library.feeds().map { it.folder }.toSet()
        assertTrue(folders.contains("Art"))
        assertFalse(library.feeds().any { it.folder == "Smoke" })
    }

    @Test
    fun pruneDropsSmokeWithoutASeedFile() = runBlocking {
        val library = InMemoryLibrary()
        library.upsertFeed(
            org.aetherfeed.app.domain.Feed(
                "smoke",
                "Hacker News",
                "https://hnrss.org/frontpage",
                ModuleKind.News,
                updatedAt = 1,
                folder = "Smoke",
            ),
        )
        library.upsertFeed(
            org.aetherfeed.app.domain.Feed(
                "real",
                "Local",
                "https://example.invalid/rss.xml",
                ModuleKind.News,
                updatedAt = 1,
                folder = "Art",
            ),
        )
        pruneSmokeLibrary(library)
        assertEquals(listOf("Art"), library.feeds().map { it.folder })
    }

    @Test
    fun firstLiveFeedSkipsSmoke() {
        val smoke = org.aetherfeed.app.domain.Feed(
            "s", "Hacker News", "https://hnrss.org/frontpage", ModuleKind.News, updatedAt = 1, folder = "Smoke",
        )
        val live = org.aetherfeed.app.domain.Feed(
            "r", "Local", "https://example.invalid/rss.xml", ModuleKind.News, updatedAt = 1, folder = "Art",
        )
        assertEquals("r", firstLiveFeed(listOf(smoke, live))?.id)
    }

    @Test
    fun envelopeIsNotDeleted() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dest = File(context.filesDir, SEED_LIBRARY_NAME)
        dest.writeText(
            """{"version":1,"kdf":"pbkdf2","aead":"aes-256-gcm","saltB64":"x","nonceB64":"y","ciphertextB64":"z"}""",
        )
        applySeedLibrary(context, InMemoryLibrary())
        assertTrue(dest.exists())
    }
}

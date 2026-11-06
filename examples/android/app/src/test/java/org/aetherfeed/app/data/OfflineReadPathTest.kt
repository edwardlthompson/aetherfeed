package org.aetherfeed.app.data

import java.io.File
import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.PlaybackPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OfflineReadPathTest {
    @get:Rule
    val tmp: TemporaryFolder = TemporaryFolder()

    @Test
    fun cachedBodyReadableWithRadioOffAndSurvivesReload() = runBlocking {
        val root = tmp.newFolder("vault")
        val path = OfflineReadPath(root)
        path.cacheForOffline(
            Article("a1", "f1", "Title", "https://example.test/a1"),
            body = "offline body",
        )
        path.starLocal("a1", ModuleKind.News, now = 10L)
        path.savePlayback(PlaybackPosition("ep1", positionMs = 12_000, durationMs = 60_000, updatedAt = 11L))

        assertEquals("offline body", path.radioOffBody("a1"))
        assertEquals(1, path.stars().size)
        assertEquals(12_000L, path.playback("ep1")?.positionMs)

        val reopened = OfflineReadPath(File(root.absolutePath))
        assertEquals("offline body", reopened.radioOffBody("a1"))
        assertEquals("a1", reopened.stars().single().targetId)
        assertEquals(12_000L, reopened.playback("ep1")?.positionMs)
        assertTrue(reopened.cachedArticles().single().localPath != null)
        assertNull(reopened.radioOffBody("missing"))
    }
}

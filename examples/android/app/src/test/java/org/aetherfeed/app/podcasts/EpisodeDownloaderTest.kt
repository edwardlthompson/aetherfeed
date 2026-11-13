package org.aetherfeed.app.podcasts

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.domain.Episode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class EpisodeDownloaderTest {
  @Test
  fun queueThenReadyUnderFilesDir() =
      runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val root = File(context.filesDir, "podcasts")
        val downloader = EpisodeDownloader(root)
        val episode =
            Episode(
                id = "ep-dl-1",
                showId = "show-1",
                title = "Episode",
                enclosureUrl = "https://example.test/audio.mp3",
            )

        assertEquals(EpisodeDownloadState.None, downloader.stateFor(episode.id))
        downloader.enqueue(episode)
        assertEquals(EpisodeDownloadState.Queued, downloader.stateFor(episode.id))

        downloader.completeQueued()
        val ready = downloader.stateFor(episode.id)
        assertTrue(ready is EpisodeDownloadState.Ready)
        val path = (ready as EpisodeDownloadState.Ready).localPath
        assertTrue(path.startsWith(root.absolutePath))
        assertTrue(File(path).isFile)
      }
}

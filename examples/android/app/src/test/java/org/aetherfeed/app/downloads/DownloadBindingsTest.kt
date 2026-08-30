package org.aetherfeed.app.downloads

import java.io.File
import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.domain.Episode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DownloadBindingsTest {
    @Test
    fun factoryGatesOnWifiAndKeepsFilesPrivate() = runTest {
        val filesDir = File.createTempFile("af-dl-bind", ".dir").also {
            it.delete()
            it.mkdirs()
        }
        try {
            val queue = createAppDownloadQueue(filesDir) { false }
            queue.enqueue(Episode("ep-1", "show-1", "Episode", "https://cdn.example/ep.mp3"))
            assertNull(queue.processNext())
            assertEquals(DownloadStatus.Queued, queue.job("ep-1")?.status)
            val leftover = File(filesDir, "episodes").list()?.isNotEmpty() == true
            assertEquals(false, leftover)
        } finally {
            filesDir.deleteRecursively()
        }
    }
}

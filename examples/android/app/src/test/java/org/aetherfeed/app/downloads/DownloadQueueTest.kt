package org.aetherfeed.app.downloads

import java.io.File
import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.domain.Episode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadQueueTest {
    @Test
    fun enqueuePreservesFifoOrder() {
        val queue = queueOf()
        queue.enqueue(episode("ep-1"))
        queue.enqueue(episode("ep-2"))
        assertEquals(listOf("ep-1", "ep-2"), queue.snapshot().map { it.episodeId })
        assertEquals(DownloadStatus.Queued, queue.job("ep-1")?.status)
    }

    @Test
    fun duplicateEnqueueKeepsFirstJob() {
        val queue = queueOf()
        queue.enqueue(episode("ep-1", "https://a.example/one.mp3"))
        queue.enqueue(episode("ep-1", "https://a.example/two.mp3"))
        assertEquals(1, queue.snapshot().size)
        assertEquals("https://a.example/one.mp3", queue.job("ep-1")?.enclosureUrl)
    }

    @Test
    fun processWritesUnderAppFilesDir() = runTest {
        withTempFiles { filesDir ->
            val store = AppPrivateEpisodeStore(filesDir)
            val queue = DownloadQueue(store, { "audio".toByteArray() })
            queue.enqueue(episode("ep-1"))
            val job = queue.processNext()
            assertEquals(DownloadStatus.Completed, job?.status)
            val path = File(requireNotNull(job?.localPath))
            assertTrue(path.exists())
            assertTrue(store.isUnderFilesDir(path))
            assertTrue(path.canonicalPath.startsWith(filesDir.canonicalPath))
            assertEquals("audio", path.readText())
        }
    }

    @Test
    fun failedFetchMarksJobFailedAndLeavesNoFile() = runTest {
        withTempFiles { filesDir ->
            val store = AppPrivateEpisodeStore(filesDir)
            val queue = DownloadQueue(store, { throw EpisodeFetchException("HTTP 404") })
            queue.enqueue(episode("ep-1"))
            val job = queue.processNext()
            assertEquals(DownloadStatus.Failed, job?.status)
            assertEquals("HTTP 404", job?.error)
            assertNull(job?.localPath)
            assertFalse(store.fileFor("ep-1").exists())
        }
    }

    @Test
    fun failedJobCanBeRequeued() = runTest {
        withTempFiles { filesDir ->
            var fail = true
            val store = AppPrivateEpisodeStore(filesDir)
            val queue = DownloadQueue(store, { url ->
                if (fail) throw EpisodeFetchException("HTTP 500")
                url.toByteArray()
            })
            queue.enqueue(episode("ep-1"))
            assertEquals(DownloadStatus.Failed, queue.processNext()?.status)
            fail = false
            queue.enqueue(episode("ep-1"))
            assertEquals(DownloadStatus.Completed, queue.processNext()?.status)
        }
    }

    @Test
    fun wifiOnlySkipsWhenOffline() = runTest {
        withTempFiles { filesDir ->
            val queue = DownloadQueue(
                AppPrivateEpisodeStore(filesDir),
                { "x".toByteArray() },
                DownloadFlags(wifiOnly = true, autoDownload = true),
                onWifi = { false },
            )
            queue.enqueue(episode("ep-1"))
            assertTrue(queue.shouldAutoEnqueue())
            assertFalse(queue.canDownloadNow())
            assertNull(queue.processNext())
            assertEquals(DownloadStatus.Queued, queue.job("ep-1")?.status)
        }
    }

    @Test
    fun enqueueRejectsBlankId() {
        val result = runCatching { queueOf().enqueue(episode("  ")) }
        assertTrue(result.isFailure)
    }
}

private fun episode(id: String, url: String = "https://cdn.example/ep.mp3") =
    Episode(id, "show-1", "Episode", url)

private fun queueOf(): DownloadQueue {
    val filesDir = File.createTempFile("af-dl-empty", ".dir").also {
        it.delete()
        it.mkdirs()
    }
    return DownloadQueue(AppPrivateEpisodeStore(filesDir), { ByteArray(1) })
}

private suspend fun withTempFiles(block: suspend (File) -> Unit) {
    val filesDir = File.createTempFile("af-dl", ".dir").also {
        it.delete()
        it.mkdirs()
    }
    try {
        block(filesDir)
    } finally {
        filesDir.deleteRecursively()
    }
}

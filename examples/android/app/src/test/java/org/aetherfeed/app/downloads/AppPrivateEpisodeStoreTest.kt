package org.aetherfeed.app.downloads

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppPrivateEpisodeStoreTest {
    @Test
    fun writeStaysUnderFilesDirAndSanitizesId() {
        withTempDir { filesDir ->
            val store = AppPrivateEpisodeStore(filesDir)
            val dest = store.write("../Download/ep 1", "bytes".toByteArray())
            assertTrue(store.isUnderFilesDir(dest))
            assertEquals("bytes", dest.readText())
            assertEquals(".._Download_ep_1.bin", dest.name)
            assertEquals("episodes", dest.parentFile?.name)
            assertFalse(dest.canonicalPath.contains("${File.separator}Download${File.separator}ep"))
        }
    }

    @Test
    fun deleteRemovesWrittenFile() {
        withTempDir { filesDir ->
            val store = AppPrivateEpisodeStore(filesDir)
            store.write("ep-1", byteArrayOf(1, 2, 3))
            assertTrue(store.fileFor("ep-1").exists())
            assertTrue(store.delete("ep-1"))
            assertFalse(store.fileFor("ep-1").exists())
        }
    }

    @Test
    fun writeRejectsEmptyBytes() {
        withTempDir { filesDir ->
            val result = runCatching { AppPrivateEpisodeStore(filesDir).write("ep-1", ByteArray(0)) }
            assertTrue(result.isFailure)
        }
    }
}

private fun withTempDir(block: (File) -> Unit) {
    val filesDir = File.createTempFile("af-store", ".dir").also {
        it.delete()
        it.mkdirs()
    }
    try {
        block(filesDir)
    } finally {
        filesDir.deleteRecursively()
    }
}

package org.aetherfeed.app.applock

import java.io.File
import android.util.Base64
import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.domain.AppLockSecretKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class EncryptedCacheTest {
    @Test
    fun migratesV1AndV2ToBinaryV3() {
        val dir = File.createTempFile("af-cache", ".dir").also {
            it.delete()
            it.mkdirs()
        }
        try {
            SessionPlainCache.clear()
            val key = ByteArray(32) { 7 }
            val cache = EncryptedCache(dir) { key }
            val folder = File(dir, "articles").also { it.mkdirs() }
            File(folder, "old.enc").writeBytes(
                wrapBytes("legacy-body".toByteArray(), Base64.encodeToString(key, Base64.NO_WRAP)),
            )
            assertEquals("legacy-body", cache.read("articles", "old")?.toString(Charsets.UTF_8))
            SessionPlainCache.clear()
            val migrated = File(folder, "old.enc").readBytes()
            assertEquals(3, cacheVersion(migrated))
            assertEquals(0x41, migrated[0].toInt())
            File(folder, "v2.enc").writeBytes(wrapCacheV2("v2-body".toByteArray(), key))
            assertEquals("v2-body", cache.read("articles", "v2")?.toString(Charsets.UTF_8))
            SessionPlainCache.clear()
            assertEquals(3, cacheVersion(File(folder, "v2.enc").readBytes()))
            cache.write("articles", "fresh", "fresh-body".toByteArray())
            assertEquals(3, cacheVersion(File(folder, "fresh.enc").readBytes()))
            assertEquals("fresh-body", cache.read("articles", "fresh")?.toString(Charsets.UTF_8))
        } finally {
            SessionPlainCache.clear()
            dir.deleteRecursively()
        }
    }

    @Test
    fun lockClearsSessionPlaintext() = runTest {
        val dir = File.createTempFile("af-sess", ".dir").also {
            it.delete()
            it.mkdirs()
        }
        try {
            SessionPlainCache.clear()
            val lock = AppLockSession(dir)
            lock.setSecret("123456", AppLockSecretKind.Pin)
            val cache = EncryptedCache(dir) { lock.sessionVaultKey() }
            cache.write("articles", "a1", "<p>Hi</p>".toByteArray())
            assertEquals("<p>Hi</p>", SessionPlainCache.utf8("articles", "a1"))
            lock.lock()
            assertNull(SessionPlainCache.utf8("articles", "a1"))
        } finally {
            SessionPlainCache.clear()
            dir.deleteRecursively()
        }
    }
}

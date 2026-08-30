package org.aetherfeed.app.applock

import java.io.File
import kotlinx.coroutines.test.runTest
import org.aetherfeed.app.domain.AppLockSecretKind
import org.aetherfeed.app.domain.AppLockState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])

class AppLockSessionTest {
    @Test
    fun setUnlockAndEncryptCache() = runTest {
        val dir = File.createTempFile("af-lock", ".dir").also {
            it.delete()
            it.mkdirs()
        }
        try {
            val lock = AppLockSession(dir)
            lock.setSecret("123456", AppLockSecretKind.Pin)
            assertEquals(AppLockState.Unlocked, lock.state())
            assertEquals(AppLockSecretKind.Pin, lock.storedKind())
            val cache = EncryptedCache(dir) { lock.sessionVaultKey() }
            val file = cache.write("articles", "a1", "<p>Hi</p>".toByteArray())
            assertFalse(file.readText().contains("<p>Hi</p>"))
            assertEquals("<p>Hi</p>", cache.read("articles", "a1")?.toString(Charsets.UTF_8))
            lock.lock()
            assertEquals(AppLockState.Locked, lock.state())
            assertFalse(lock.unlock("000000"))
            assertTrue(lock.unlock("123456"))
        } finally {
            dir.deleteRecursively()
        }
    }
}

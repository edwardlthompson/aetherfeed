package org.aetherfeed.app.applock

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.aetherfeed.app.domain.APP_LOCK_BACKOFF_AFTER
import org.aetherfeed.app.domain.APP_LOCK_TIMEOUT_MS
import org.aetherfeed.app.domain.AppLock
import org.aetherfeed.app.domain.AppLockSecretKind
import org.aetherfeed.app.domain.AppLockState
import org.aetherfeed.app.domain.classifyLockSecret

class AppLockSession(
    private val filesDir: File,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val seedVaultKey: () -> ByteArray? = { null },
) : AppLock {
    private var sessionKey: ByteArray? = null
    private var unlockedAt = 0L
    private var failures = 0
    private var backoffUntil = 0L

    private fun wrapFile(): File = File(filesDir, "lock-wrap.json")

    fun sessionVaultKey(): ByteArray? {
        val key = sessionKey ?: return null
        if (unlockedAt > 0 && clock() - unlockedAt >= APP_LOCK_TIMEOUT_MS) {
            sessionKey = null
            SessionPlainCache.clear()
            return null
        }
        return key
    }

    override suspend fun setSecret(secret: String, kind: AppLockSecretKind) = withContext(Dispatchers.Default) {
        require(classifyLockSecret(secret) == kind) { "weak secret" }
        val vault = seedVaultKey()?.takeIf { it.size == 32 }
            ?: ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        wrapFile().writeBytes(wrapBytes(vault, secret, kind))
        sessionKey = vault
        unlockedAt = clock()
        failures = 0
    }

    override suspend fun unlock(secret: String): Boolean = withContext(Dispatchers.Default) {
        if (clock() < backoffUntil) return@withContext false
        val file = wrapFile()
        if (!file.exists()) return@withContext false
        runCatching {
            sessionKey = openBytes(file.readBytes(), secret)
            unlockedAt = clock()
            failures = 0
            true
        }.getOrElse {
            failures += 1
            if (failures >= APP_LOCK_BACKOFF_AFTER) {
                backoffUntil = clock() + (1 shl failures.coerceAtMost(8)) * 1000L
            }
            sessionKey = null
            false
        }
    }

    override fun lock() {
        sessionKey = null
        unlockedAt = 0
        SessionPlainCache.clear()
    }

    override suspend fun wipe() = withContext(Dispatchers.IO) {
        lock()
        failures = 0
        wrapFile().delete()
        File(filesDir, "articles").deleteRecursively()
        File(filesDir, "images").deleteRecursively()
        File(filesDir, "episodes").deleteRecursively()
        Unit
    }

    override fun state(): AppLockState {
        if (!wrapFile().exists()) return AppLockState.Unset
        return if (sessionVaultKey() != null) AppLockState.Unlocked else AppLockState.Locked
    }

    fun storedKind(): AppLockSecretKind? {
        val file = wrapFile()
        if (!file.exists()) return null
        return runCatching { peekLockKind(file.readBytes()) }.getOrNull()
    }
}

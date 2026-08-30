package org.aetherfeed.app.domain

enum class AppLockSecretKind { Pin, Passphrase }

enum class AppLockState { Unset, Locked, Unlocked }

interface AppLock {
    suspend fun setSecret(secret: String, kind: AppLockSecretKind)
    suspend fun unlock(secret: String): Boolean
    fun lock()
    suspend fun wipe()
    fun state(): AppLockState
}

const val APP_LOCK_TIMEOUT_MS = 120_000L
const val APP_LOCK_PIN_MIN = 6
const val APP_LOCK_PASSPHRASE_MIN = 8
const val APP_LOCK_BACKOFF_AFTER = 5

fun classifyLockSecret(secret: String): AppLockSecretKind? {
    val trimmed = secret.trim()
    if (trimmed.all { it.isDigit() } && trimmed.length >= APP_LOCK_PIN_MIN) return AppLockSecretKind.Pin
    if (trimmed.length >= APP_LOCK_PASSPHRASE_MIN) return AppLockSecretKind.Passphrase
    return null
}

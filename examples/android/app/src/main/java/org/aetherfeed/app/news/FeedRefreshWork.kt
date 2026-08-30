package org.aetherfeed.app.news

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import org.aetherfeed.app.applock.AppLockHolder
import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.data.RoomLibraryRepository
import org.aetherfeed.app.data.SqlCipherVault
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.downloads.isUnmeteredNetwork

private const val UNIQUE_REFRESH = "feed-refresh"
private const val KEY_FORCE = "force"

object FeedRefreshScheduler {
    fun ensureDefault(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_REFRESH,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<FeedRefreshWorker>(
                REFRESH_INTERVAL_DEFAULT.toLong(),
                TimeUnit.HOURS,
            ).build(),
        )
    }

    fun updateInterval(context: Context, hours: Int) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_REFRESH,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<FeedRefreshWorker>(
                clampInterval(hours).toLong(),
                TimeUnit.HOURS,
            ).build(),
        )
    }

    fun enqueueNow(context: Context) {
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<FeedRefreshWorker>()
                .setInputData(workDataOf(KEY_FORCE to true))
                .build(),
        )
    }
}

class FeedRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (AppLockHolder.vaultKey() == null) return Result.success()
        val prefs = FeedRefreshPrefs(applicationContext)
        if (!canFetchNews(prefs.currentWifiOnly(), isUnmeteredNetwork(applicationContext))) {
            return Result.success()
        }
        val db = runCatching { SqlCipherVault().open(applicationContext) }.getOrNull()
            ?: return Result.success()
        val library = RoomLibraryRepository(db.libraryDao())
        val index = EncryptedArticleIndex(
            EncryptedCache(applicationContext.filesDir) { AppLockHolder.vaultKey() },
        )
        val repo = VaultNewsRepository(library, HttpFeedFetcher(), policy = { prefs.current() }, index = index)
        val force = inputData.getBoolean(KEY_FORCE, false)
        val intervalMs = prefs.currentIntervalHours() * 3_600_000L
        val now = System.currentTimeMillis()
        val deadline = now + 9 * 60_000L
        for (feed in library.feeds().filter { it.kind == ModuleKind.News }) {
            if (System.currentTimeMillis() > deadline) return Result.retry()
            if (!force && now - feed.updatedAt < intervalMs) continue
            runCatching { repo.refresh(feed.id) }
        }
        return Result.success()
    }
}

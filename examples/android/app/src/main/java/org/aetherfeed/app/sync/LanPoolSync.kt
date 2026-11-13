package org.aetherfeed.app.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class LanPoolStatus(
    val message: String = "LAN pool idle",
    val ok: Boolean = true,
)

/** Pull/merge/push encrypted pool document against desktop LAN hub. */
class LanPoolSync(
    private val library: org.aetherfeed.app.domain.LibraryRepository,
    private val prefs: LanPoolPrefs,
    private val feedChangeNotifier: FeedChangeNotifier? = null,
) {
    private val statusFlow = MutableStateFlow(LanPoolStatus())
    val status: StateFlow<LanPoolStatus> = statusFlow.asStateFlow()

    suspend fun syncNow(attemptsLeft: Int = 3) {
        val config = prefs.load()
        if (config.hubUrl.isBlank() || config.passphrase.isBlank()) {
            statusFlow.value = LanPoolStatus(message = "Set hub URL and passphrase", ok = true)
            return
        }
        try {
            withContext(Dispatchers.IO) { syncOnce(config) }
        } catch (e: Exception) {
            if (attemptsLeft > 1 && e.message?.contains("412") == true) {
                // Concurrent writers (phone + tablet + laptop) race on If-Match.
                syncNow(attemptsLeft = attemptsLeft - 1)
                return
            }
            statusFlow.value =
                LanPoolStatus(
                    message = "LAN sync failed: ${e.message ?: e.javaClass.simpleName}",
                    ok = false,
                )
        }
    }

    private suspend fun syncOnce(config: LanPoolConfig) {
        val pool = LanPoolHttp.getPool(config.hubUrl)
        val (etag, saltHint) =
            when (pool) {
                is LanHttpResult.Ok -> pool.etag to pool.authSaltB64
                is LanHttpResult.Empty -> pool.etag to pool.authSaltB64
                is LanHttpResult.Failed ->
                    throw IllegalStateException("HTTP ${pool.code}: ${pool.message}")
            }
        val authSalt =
            saltHint.ifBlank {
                LanPoolHttp.getMeta(config.hubUrl).authSaltB64
            }
        val remoteDoc =
            when (pool) {
                is LanHttpResult.Empty -> PoolDocument(version = SubscriptionPoolCodec.VERSION)
                is LanHttpResult.Ok -> {
                    val plain = LanPoolCrypto.openEnvelope(config.passphrase, pool.body)
                    SubscriptionPoolCodec.decode(String(plain, Charsets.UTF_8))
                }
                is LanHttpResult.Failed -> error("unreachable")
            }
        val local = LanPoolApply.snapshotLocal(library)
        val merged = PoolMerge.mergeDocuments(local, remoteDoc)
        LanPoolApply.applyDocument(library, merged, feedChangeNotifier)
        val now = System.currentTimeMillis()
        val toPush = merged.copy(version = SubscriptionPoolCodec.VERSION, updatedAt = now)
        val envelope =
            LanPoolCrypto.sealEnvelope(
                config.passphrase,
                SubscriptionPoolCodec.encode(toPush).toByteArray(Charsets.UTF_8),
            )
        val bearer = LanPoolCrypto.putBearer(config.passphrase, authSalt, etag)
        when (val put = LanPoolHttp.putPool(config.hubUrl, envelope, etag, bearer)) {
            is LanHttpResult.Failed ->
                throw IllegalStateException("HTTP ${put.code}: ${put.message}")
            else ->
                statusFlow.value =
                    LanPoolStatus(message = "LAN synced (${toPush.subs.size} feeds)", ok = true)
        }
    }
}

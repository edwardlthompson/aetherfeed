package org.aetherfeed.app.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository

class SubscriptionPoolSync(
    private val library: LibraryRepository,
    private val client: SubscriptionPoolGateway,
    private val feedChangeNotifier: FeedChangeNotifier? = null,
) {
    private val statusFlow = MutableStateFlow(HermesPoolStatus())
    val status: StateFlow<HermesPoolStatus> = statusFlow.asStateFlow()

    suspend fun pullAndMerge() {
        when (val result = client.read()) {
            is PoolClientResult.Unavailable -> {
                statusFlow.value = HermesPoolStatus(message = result.reason, ok = true)
                return
            }
            is PoolClientResult.Denied -> {
                statusFlow.value = HermesPoolStatus(message = "Permission denied — share signing key", ok = false)
                return
            }
            is PoolClientResult.Invalid -> {
                statusFlow.value = HermesPoolStatus(message = "Invalid pool document; local unchanged", ok = false)
                return
            }
            is PoolClientResult.Ok -> {
                val localFeeds = library.feeds()
                val localPool = localFeeds.toPoolSubs()
                val merged = SubscriptionPoolCodec.mergeLww(localPool, result.document.subs)
                val apply = suspend { applyMerged(localFeeds, merged) }
                if (feedChangeNotifier != null) {
                    feedChangeNotifier.suppress { apply() }
                } else {
                    apply()
                }
                val now = System.currentTimeMillis()
                val publish =
                    client.write(
                        PoolDocument(
                            version = SubscriptionPoolCodec.VERSION,
                            updatedAt = now,
                            subs = merged,
                            reads = result.document.reads,
                            stars = result.document.stars,
                            playback = result.document.playback,
                        ),
                    )
                statusFlow.value =
                    when (publish) {
                        is PoolClientResult.Ok ->
                            HermesPoolStatus(
                                message = "Synced with Hermes (${merged.size} feeds)",
                                lastSyncAt = now,
                                ok = true,
                            )
                        is PoolClientResult.Denied ->
                            HermesPoolStatus(message = "Permission denied — share signing key", ok = false)
                        is PoolClientResult.Unavailable ->
                            HermesPoolStatus(message = publish.reason, ok = true)
                        is PoolClientResult.Invalid ->
                            HermesPoolStatus(message = publish.reason, ok = false)
                    }
            }
        }
    }

    suspend fun publishFromLocal() {
        val now = System.currentTimeMillis()
        val stamped =
            library.feeds().toPoolSubs().map { it.copy(updatedAt = now) }
        when (
            val result =
                client.write(
                    PoolDocument(
                        version = SubscriptionPoolCodec.VERSION,
                        updatedAt = now,
                        subs = stamped,
                    ),
                )
        ) {
            is PoolClientResult.Ok ->
                statusFlow.value =
                    HermesPoolStatus(
                        message = "Published ${stamped.size} feeds to Hermes",
                        lastSyncAt = now,
                        ok = true,
                    )
            is PoolClientResult.Denied ->
                statusFlow.value =
                    HermesPoolStatus(message = "Permission denied — share signing key", ok = false)
            is PoolClientResult.Unavailable ->
                statusFlow.value = HermesPoolStatus(message = result.reason, ok = true)
            is PoolClientResult.Invalid ->
                statusFlow.value = HermesPoolStatus(message = result.reason, ok = false)
        }
    }

    private suspend fun applyMerged(prior: List<Feed>, merged: List<PoolSub>) {
        val byUrl = prior.associateBy { SubscriptionPoolCodec.normalizeUrl(it.url) }
        for (pool in merged) {
            val kind = pool.kind.toModuleKind() ?: continue
            val existing = byUrl[pool.url]
            library.upsertFeed(
                Feed(
                    id = existing?.id ?: pool.url,
                    title = pool.title.ifBlank { existing?.title ?: pool.url },
                    url = pool.url,
                    kind = kind,
                    siteUrl = existing?.siteUrl,
                    updatedAt = pool.updatedAt.takeIf { it > 0L } ?: System.currentTimeMillis(),
                ),
            )
        }
    }
}

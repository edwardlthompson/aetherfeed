package org.aetherfeed.app.sync

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.PlaybackPosition
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus
import org.aetherfeed.app.domain.Star

internal object LanPoolApply {
    suspend fun applyDocument(
        library: LibraryRepository,
        doc: PoolDocument,
        feedChangeNotifier: FeedChangeNotifier?,
    ) {
        val apply = suspend {
            applySubs(library, doc.subs)
            applyReads(library, doc.reads)
            applyStars(library, doc.stars)
            applyPlayback(library, doc.playback)
        }
        if (feedChangeNotifier != null) {
            feedChangeNotifier.suppress { apply() }
        } else {
            apply()
        }
    }

    suspend fun snapshotLocal(library: LibraryRepository): PoolDocument {
        val feeds = library.feeds()
        val articles = library.articles(null)
        return PoolDocument(
            version = SubscriptionPoolCodec.VERSION,
            updatedAt = feeds.maxOfOrNull { it.updatedAt } ?: 0L,
            subs = feeds.toPoolSubs(),
            reads =
                library.readStates().map {
                    PoolRead(
                        url = LanPoolTargets.poolUrl(articles, it.targetId),
                        read = it.status == ReadStatus.Read,
                        updatedAt = it.updatedAt,
                    )
                },
            stars =
                library.stars().map {
                    PoolStar(
                        url = LanPoolTargets.poolUrl(articles, it.targetId),
                        starred = true,
                        updatedAt = it.createdAt,
                    )
                },
            playback =
                library.playbackAll().map {
                    PoolPlayback(
                        url = SubscriptionPoolCodec.normalizeUrl(it.episodeId),
                        positionMs = it.positionMs,
                        updatedAt = it.updatedAt,
                    )
                },
        )
    }

    private suspend fun applySubs(library: LibraryRepository, merged: List<PoolSub>) {
        val prior = library.feeds().associateBy { SubscriptionPoolCodec.normalizeUrl(it.url) }
        for (pool in merged) {
            val kind = pool.kind.toModuleKind() ?: continue
            val existing = prior[pool.url]
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

    private suspend fun applyReads(library: LibraryRepository, rows: List<PoolRead>) {
        val articles = library.articles(null)
        for (row in rows) {
            library.upsertReadState(
                ReadState(
                    targetId = LanPoolTargets.localId(articles, row.url),
                    module = ModuleKind.News,
                    status = if (row.read) ReadStatus.Read else ReadStatus.Unread,
                    updatedAt = row.updatedAt,
                ),
            )
        }
    }

    private suspend fun applyStars(library: LibraryRepository, rows: List<PoolStar>) {
        val articles = library.articles(null)
        for (row in rows) {
            val target = LanPoolTargets.localId(articles, row.url)
            if (row.starred) {
                library.upsertStar(Star(targetId = target, module = ModuleKind.News, createdAt = row.updatedAt))
            } else {
                library.removeStar(target)
            }
        }
    }

    private suspend fun applyPlayback(library: LibraryRepository, rows: List<PoolPlayback>) {
        for (row in rows) {
            library.upsertPlayback(
                PlaybackPosition(
                    episodeId = row.url,
                    positionMs = row.positionMs,
                    durationMs = null,
                    updatedAt = row.updatedAt,
                ),
            )
        }
    }

    private fun List<Feed>.toPoolSubs(): List<PoolSub> =
        map { feed ->
            val kind =
                when (feed.kind) {
                    ModuleKind.News -> SubscriptionPoolCodec.KIND_NEWS
                    ModuleKind.Podcast -> SubscriptionPoolCodec.KIND_PODCAST
                    ModuleKind.Booru -> SubscriptionPoolCodec.KIND_BOORU
                }
            PoolSub(
                url = SubscriptionPoolCodec.normalizeUrl(feed.url),
                title = feed.title,
                kind = kind,
                updatedAt = feed.updatedAt,
            )
        }

    private fun String.toModuleKind(): ModuleKind? =
        when (this) {
            SubscriptionPoolCodec.KIND_NEWS -> ModuleKind.News
            SubscriptionPoolCodec.KIND_PODCAST -> ModuleKind.Podcast
            SubscriptionPoolCodec.KIND_BOORU -> ModuleKind.Booru
            else -> null
        }
}

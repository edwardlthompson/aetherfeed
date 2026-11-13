package org.aetherfeed.app.sync

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind

internal fun List<Feed>.toPoolSubs(): List<PoolSub> =
    mapNotNull { feed ->
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
            tag = "",
            updatedAt = feed.updatedAt,
        )
    }

internal fun String.toModuleKind(): ModuleKind? =
    when (this) {
        SubscriptionPoolCodec.KIND_NEWS -> ModuleKind.News
        SubscriptionPoolCodec.KIND_PODCAST -> ModuleKind.Podcast
        SubscriptionPoolCodec.KIND_BOORU -> ModuleKind.Booru
        else -> null
    }

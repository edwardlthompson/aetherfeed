package org.aetherfeed.app.sync

/** LWW merge helpers for pool document rows. */
object PoolMerge {
    fun mergeSubs(local: List<PoolSub>, incoming: List<PoolSub>): List<PoolSub> {
        val map = LinkedHashMap<String, PoolSub>()
        for (row in local) {
            val n = SubscriptionPoolCodec.normalizeSub(row)
            map[n.url] = n
        }
        for (row in incoming) {
            val n = SubscriptionPoolCodec.normalizeSub(row)
            val prior = map[n.url]
            if (prior == null || n.updatedAt > prior.updatedAt) {
                map[n.url] = n
            }
        }
        return map.values.toList()
    }

    fun mergeReads(local: List<PoolRead>, incoming: List<PoolRead>): List<PoolRead> {
        val map = LinkedHashMap<String, PoolRead>()
        for (row in local) {
            val n = row.copy(url = SubscriptionPoolCodec.normalizeUrl(row.url))
            map[n.url] = n
        }
        for (row in incoming) {
            val n = row.copy(url = SubscriptionPoolCodec.normalizeUrl(row.url))
            val prior = map[n.url]
            if (prior == null || n.updatedAt > prior.updatedAt) {
                map[n.url] = n
            } else if (n.updatedAt == prior.updatedAt && n.read && !prior.read) {
                map[n.url] = n
            }
        }
        return map.values.toList()
    }

    fun mergeStars(local: List<PoolStar>, incoming: List<PoolStar>): List<PoolStar> {
        val map = LinkedHashMap<String, PoolStar>()
        for (row in local) {
            val n = row.copy(url = SubscriptionPoolCodec.normalizeUrl(row.url))
            map[n.url] = n
        }
        for (row in incoming) {
            val n = row.copy(url = SubscriptionPoolCodec.normalizeUrl(row.url))
            val prior = map[n.url]
            if (prior == null || n.updatedAt > prior.updatedAt) {
                map[n.url] = n
            } else if (n.updatedAt == prior.updatedAt && n.starred && !prior.starred) {
                map[n.url] = n
            }
        }
        return map.values.toList()
    }

    fun mergePlayback(
        local: List<PoolPlayback>,
        incoming: List<PoolPlayback>,
    ): List<PoolPlayback> {
        val map = LinkedHashMap<String, PoolPlayback>()
        for (row in local) {
            val n = row.copy(url = SubscriptionPoolCodec.normalizeUrl(row.url))
            map[n.url] = n
        }
        for (row in incoming) {
            val n = row.copy(url = SubscriptionPoolCodec.normalizeUrl(row.url))
            val prior = map[n.url]
            if (prior == null || n.updatedAt > prior.updatedAt) {
                map[n.url] = n
            } else if (n.updatedAt == prior.updatedAt && n.positionMs > prior.positionMs) {
                map[n.url] = n
            }
        }
        return map.values.toList()
    }

    fun mergeDocuments(local: PoolDocument, incoming: PoolDocument): PoolDocument {
        return PoolDocument(
            version = SubscriptionPoolCodec.VERSION,
            updatedAt = maxOf(local.updatedAt, incoming.updatedAt),
            subs = mergeSubs(local.subs, incoming.subs),
            reads = mergeReads(local.reads, incoming.reads),
            stars = mergeStars(local.stars, incoming.stars),
            playback = mergePlayback(local.playback, incoming.playback),
        )
    }
}

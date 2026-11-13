package org.aetherfeed.app.sync

import org.aetherfeed.app.domain.Article

/** Map pool URL keys to local article ids (guid ≠ permalink). */
internal object LanPoolTargets {
    fun localId(articles: List<Article>, poolUrl: String): String {
        val n = SubscriptionPoolCodec.normalizeUrl(poolUrl)
        return articles
            .firstOrNull {
                SubscriptionPoolCodec.normalizeUrl(it.url) == n ||
                    SubscriptionPoolCodec.normalizeUrl(it.id) == n
            }?.id ?: n
    }

    fun poolUrl(articles: List<Article>, targetId: String): String {
        val hit = articles.firstOrNull { it.id == targetId }
        return SubscriptionPoolCodec.normalizeUrl(hit?.url ?: targetId)
    }
}

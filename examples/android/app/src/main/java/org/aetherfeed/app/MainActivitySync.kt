package org.aetherfeed.app

import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.news.NewsFeedRefresh
import org.aetherfeed.app.notify.ArticleNotificationPoster
import org.aetherfeed.app.notify.notifyOnUnreadDelta
import org.aetherfeed.app.sync.LanPoolSync
import org.aetherfeed.app.sync.ReadReceiptSync
import org.aetherfeed.app.sync.SubscriptionPoolSync

/** Shared pull/refresh/notify sequence for [MainActivity] onCreate/onResume. */
suspend fun runLibrarySyncPass(
    libraryRepository: LibraryRepository,
    subscriptionPoolSync: SubscriptionPoolSync,
    lanPoolSync: LanPoolSync,
    readReceiptSync: ReadReceiptSync,
    articleNotificationPoster: ArticleNotificationPoster,
) {
    subscriptionPoolSync.pullAndMerge()
    lanPoolSync.syncNow()
    val unreadBefore = libraryRepository.unreadCount()
    NewsFeedRefresh.refresh(libraryRepository)
    notifyOnUnreadDelta(libraryRepository, unreadBefore, articleNotificationPoster)
    readReceiptSync.pullAndApply()
    lanPoolSync.syncNow()
}

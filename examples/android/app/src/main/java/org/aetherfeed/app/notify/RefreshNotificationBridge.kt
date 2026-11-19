package org.aetherfeed.app.notify

import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.shared.domain.notify.formatNewArticlesNotice

suspend fun notifyOnUnreadDelta(
    library: LibraryRepository,
    before: Int,
    poster: ArticleNotificationPoster,
) {
    val after = library.unreadCount()
    val delta = after - before
    formatNewArticlesNotice(delta)?.let { poster.post(it) }
}
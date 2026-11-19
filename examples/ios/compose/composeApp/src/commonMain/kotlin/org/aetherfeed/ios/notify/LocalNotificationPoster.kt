package org.aetherfeed.ios.notify

import org.aetherfeed.shared.domain.notify.ArticleNotice

/** Local-only notices (no APNs). Darwin `UNUserNotificationCenter` wiring lands on iosMain/Mac. */
interface LocalNotificationPoster {
    fun post(notice: ArticleNotice)
}

class JvmLocalNotificationPoster : LocalNotificationPoster {
    var last: ArticleNotice? = null
        private set

    override fun post(notice: ArticleNotice) {
        last = notice
    }
}
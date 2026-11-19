package org.aetherfeed.ios

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.aetherfeed.ios.notify.JvmLocalNotificationPoster
import org.aetherfeed.ios.sync.LanPoolClientStub
import org.aetherfeed.shared.domain.notify.formatNewArticlesNotice
import org.aetherfeed.shared.domain.sync.LanPoolSettings

class IosSprint61Test {
    @Test
    fun localNotificationPosterCapturesNotice() {
        val poster = JvmLocalNotificationPoster()
        val notice = formatNewArticlesNotice(2, "Feeds")
        assertNotNull(notice)
        poster.post(notice)
        assertEquals(notice, poster.last)
    }

    @Test
    fun lanPoolClientRequiresHubAndPassphrase() {
        val client = LanPoolClientStub()
        assertEquals("Set hub URL and passphrase", client.syncNow())
        client.configure(LanPoolSettings(hubUrl = "http://127.0.0.1:8787", passphrase = "secret"))
        assertEquals("LAN sync queued (stub client).", client.syncNow())
    }
}
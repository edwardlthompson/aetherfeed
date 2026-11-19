package org.aetherfeed.ios

import org.aetherfeed.ios.refresh.shouldRefreshOnForeground
import org.aetherfeed.ios.refresh.BackgroundRefreshPolicy
import org.aetherfeed.ios.widget.WidgetUnreadWritePlan
import org.aetherfeed.ios.widget.encodedUtf8
import org.aetherfeed.shared.domain.unread.IOS_APP_GROUP_ID
import org.aetherfeed.shared.domain.unread.IOS_WIDGET_UNREAD_FILENAME
import org.aetherfeed.shared.domain.unread.WidgetUnreadSnapshot
import org.aetherfeed.ios.vault.createIosVaultKeychain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IosSprint60Test {
    @Test
    fun foregroundRefreshAlwaysWhenEnabled() {
        assertTrue(shouldRefreshOnForeground(BackgroundRefreshPolicy()))
    }

    @Test
    fun widgetWritePlanUsesAppGroupContract() {
        val plan =
            WidgetUnreadWritePlan(
                appGroupId = IOS_APP_GROUP_ID,
                relativeFilename = IOS_WIDGET_UNREAD_FILENAME,
                snapshot = WidgetUnreadSnapshot(unreadTotal = 3, updatedAtEpochMs = 42),
            )
        assertEquals(
            """{"version":1,"unreadTotal":3,"updatedAtEpochMs":42}""",
            plan.encodedUtf8(),
        )
    }

    @Test
    fun keychainJvmStubRoundTrip() {
        val kc = createIosVaultKeychain()
        kc.storeSecret("lan_passphrase", byteArrayOf(9, 9))
        assertNotNull(kc.loadSecret("lan_passphrase"))
    }
}
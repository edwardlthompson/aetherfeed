package org.aetherfeed.app.notify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UnifiedPushConnectorTest {
    @Test
    fun registerIfNeededIsBestEffort() {
        val context: Context = ApplicationProvider.getApplicationContext()
        UnifiedPushConnector.registerIfNeeded(context)
    }
}
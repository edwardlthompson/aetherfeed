package org.aetherfeed.app.applock

import android.app.Activity
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class SecureRecentsTest {
    @Test
    @Config(sdk = [29])
    fun setsFlagSecureOnPreTiramisu() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        applySecureRecents(activity)
        assertTrue(isWindowSecure(activity))
    }

    @Test
    @Config(sdk = [33])
    fun setsFlagSecureOnApi33() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        applySecureRecents(activity)
        assertTrue(isWindowSecure(activity))
    }
}

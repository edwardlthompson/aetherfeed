package org.aetherfeed.app.ui.navigation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.clearPreferenceDataStores
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ShellPrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun reset() = runBlocking { context.clearPreferenceDataStores() }

    @Test
    fun remembersLastMode() = runBlocking {
        val prefs = ShellPrefs(context)
        assertEquals(AppDestination.News, prefs.current())
        prefs.setMode(AppDestination.Podcasts)
        assertEquals(AppDestination.Podcasts, prefs.current())
        assertEquals(AppDestination.News, parseAppDestination("nope"))
    }
}

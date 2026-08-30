package org.aetherfeed.app.news

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
class FeedRefreshPrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun reset() = runBlocking {
        context.clearPreferenceDataStores()
        FeedRefreshPrefs(context).clear()
    }

    @Test
    fun defaultsHourlyAndLastTen() = runBlocking {
        val prefs = FeedRefreshPrefs(context)
        assertEquals(1, prefs.currentIntervalHours())
        assertEquals(HistoryMode.Count, prefs.current().mode)
        assertEquals(10, prefs.current().count)
    }

    @Test
    fun persistsIntervalAndDayHistory() = runBlocking {
        val prefs = FeedRefreshPrefs(context)
        prefs.setIntervalHours(6)
        prefs.setHistoryMode(HistoryMode.Days)
        prefs.setHistoryDays(30)
        assertEquals(6, prefs.currentIntervalHours())
        assertEquals(HistoryMode.Days, prefs.current().mode)
        assertEquals(30, prefs.current().days)
        assertEquals(12, clampInterval(11))
    }

    @Test
    fun defaultsWifiOnlyAndCanAllowCellular() = runBlocking {
        val prefs = FeedRefreshPrefs(context)
        assertEquals(true, prefs.currentWifiOnly())
        prefs.setWifiOnly(false)
        assertEquals(false, prefs.currentWifiOnly())
    }
}

package org.aetherfeed.app.browse

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.clearPreferenceDataStores
import org.aetherfeed.app.news.ReaderMode
import org.aetherfeed.app.ui.navigation.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class BrowsePrefsTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Before
    fun reset() {
        context.clearPreferenceDataStores()
    }

    @Test
    fun persistsModuleFiltersAndReaderChrome() = runBlocking {
        val prefs = BrowsePrefs(context)
        assertFalse(prefs.moduleState("news").first().unreadOnly)
        prefs.setUnreadOnly("news", true)
        prefs.setNodeId("news", "folder:ex.test")
        prefs.setSort("news", SortOrder.Oldest)
        prefs.setDestination(AppDestination.Podcasts)
        prefs.setReaderMode(ReaderMode.WEB)
        val news = prefs.moduleState("news").first()
        assertTrue(news.unreadOnly)
        assertEquals("folder:ex.test", news.nodeId)
        assertEquals(SortOrder.Oldest, news.sort)
        assertEquals(AppDestination.Podcasts, prefs.destination.first())
        assertEquals(ReaderMode.WEB, prefs.readerMode.first())
    }
}

package org.aetherfeed.app.news

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.clearPreferenceDataStores
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
class NewsChromePrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun reset() = runBlocking {
        context.clearPreferenceDataStores()
    }

    @Test
    fun defaultsCollapsedAndRemembersExpandHideAndWeights() = runBlocking {
        val prefs = NewsChromePrefs(context)
        val first = prefs.current()
        assertTrue(first.expanded.isEmpty())
        assertFalse(first.sidebarHidden)
        prefs.setExpanded(setOf("World"))
        prefs.setSidebarHidden(true)
        prefs.setOldestFirst(true)
        prefs.setWeights(0.5f, 0.3f, 0.2f)
        val next = prefs.current()
        assertEquals(setOf("World"), next.expanded)
        assertTrue(next.sidebarHidden)
        assertTrue(next.oldestFirst)
        assertEquals(1f, next.source + next.timeline + next.reader, 0.01f)
        prefs.setLocation("World", "feed:world")
        val here = prefs.current()
        assertEquals("World", here.folder)
        assertEquals("feed:world", here.feedId)
        assertEquals(setOf("World"), here.expanded)
        prefs.setExpanded(toggleNewsExpanded(here.expanded, "World"))
        prefs.setLocation("World", "feed:world")
        assertTrue(prefs.current().expanded.isEmpty())
        prefs.setExpanded(toggleNewsExpanded(prefs.current().expanded, "World"))
        assertEquals(setOf("World"), prefs.current().expanded)
    }

    @Test
    fun toggleExpandDoesNotForceOtherFoldersOpen() {
        assertEquals(setOf("Art"), toggleNewsExpanded(emptySet(), "Art"))
        assertEquals(emptySet<String>(), toggleNewsExpanded(setOf("Art"), "Art"))
        assertEquals(setOf("Art", "World"), toggleNewsExpanded(setOf("Art"), "World"))
        assertEquals(setOf("Art"), toggleNewsExpanded(setOf("Art", "World"), "World"))
        assertEquals(emptySet<String>(), toggleNewsExpanded(emptySet(), "  "))
    }
}

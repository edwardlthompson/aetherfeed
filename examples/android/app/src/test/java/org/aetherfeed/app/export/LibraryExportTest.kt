package org.aetherfeed.app.export

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.PlaybackPosition
import org.aetherfeed.app.domain.Star
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class LibraryExportTest {
    @Test
    fun exportLibraryJson_includesVersionAndEntities() {
        val snapshot =
            LibraryExportSnapshot(
                exportedAt = 1_700_000_000_000L,
                feeds =
                    listOf(
                        Feed(
                            id = "f1",
                            title = "Example",
                            url = "https://example.test/rss.xml",
                            kind = ModuleKind.News,
                            updatedAt = 100,
                        ),
                    ),
                stars =
                    listOf(
                        Star("a1", ModuleKind.News, 200),
                    ),
                playback =
                    listOf(
                        PlaybackPosition("ep1", 30_000, 120_000, 300),
                    ),
            )
        val json = JSONObject(exportLibraryJson(snapshot).toString(Charsets.UTF_8))
        assertEquals(1, json.getInt("version"))
        assertEquals(1, json.getJSONArray("feeds").length())
        assertEquals(1, json.getJSONArray("stars").length())
        assertEquals(1, json.getJSONArray("playback").length())
        assertEquals("f1", json.getJSONArray("feeds").getJSONObject(0).getString("id"))
    }
}

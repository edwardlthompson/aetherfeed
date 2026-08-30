package org.aetherfeed.app.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class GithubReleaseTest {
    @Test
    fun parseKeepsNamedAssets() {
        val parsed = GithubRelease.parse(
            """{"html_url":"https://github.com/edwardlthompson/aetherfeed/releases/tag/v1","assets":[{"name":"notes.md"},{"name":"aetherfeed-0.2.0-foss.apk","browser_download_url":"https://ex/app.apk"}]}""",
        )
        assertEquals(1, parsed?.assets?.size)
        assertEquals("aetherfeed-0.2.0-foss.apk", parsed?.assets?.first()?.name)
        assertEquals("https://ex/app.apk", parsed?.assets?.first()?.url)
    }

    @Test
    fun parseReturnsNullOnJunk() {
        assertNull(GithubRelease.parse("not-json"))
    }

    @Test
    fun parseEmptyAssetsStaysQuiet() {
        val parsed = GithubRelease.parse("""{"html_url":"https://ex","assets":[]}""")
        assertTrue(parsed?.assets?.isEmpty() == true)
    }
}

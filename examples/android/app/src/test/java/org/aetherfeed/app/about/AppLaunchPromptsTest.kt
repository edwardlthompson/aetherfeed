package org.aetherfeed.app.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLaunchPromptsTest {
    @Test
    fun firstRunRecordsSeenWithoutDonate() {
        val result = AppLaunchPrompts.decide(
            lastSeenVersion = null,
            currentVersion = "0.1.0",
            lastCheckAt = null,
            dismissedVersion = null,
            now = 0L,
            checksEnabled = false,
            release = null,
        )
        assertNull(result.prompt)
        assertTrue(result.seen)
        assertFalse(result.checked)
    }

    @Test
    fun versionChangeReturnsDonateAndSkipsFetch() {
        val result = AppLaunchPrompts.decide(
            lastSeenVersion = "0.1.0",
            currentVersion = "0.2.0",
            lastCheckAt = 0L,
            dismissedVersion = null,
            now = ProductUpdate.MS_DAY,
            checksEnabled = true,
            release = GithubRelease.Parsed(
                htmlUrl = DonateLinks.RELEASES_PAGE,
                assets = listOf(
                    ProductUpdate.NamedAsset("aetherfeed-0.3.0-foss.apk", "https://ex/app.apk"),
                ),
            ),
        )
        assertEquals(AppLaunchPrompts.Prompt.Donate, result.prompt)
        assertFalse(result.seen)
        assertFalse(result.checked)
    }

    @Test
    fun newerApkPromptsInstall() {
        val result = AppLaunchPrompts.decide(
            lastSeenVersion = "0.1.0",
            currentVersion = "0.1.0",
            lastCheckAt = null,
            dismissedVersion = null,
            now = 0L,
            checksEnabled = true,
            release = GithubRelease.Parsed(
                htmlUrl = DonateLinks.RELEASES_PAGE,
                assets = listOf(
                    ProductUpdate.NamedAsset("aetherfeed-0.2.0-foss.apk", "https://ex/app.apk"),
                ),
            ),
        )
        assertEquals(AppLaunchPrompts.Prompt.Update("0.2.0", "https://ex/app.apk"), result.prompt)
        assertTrue(result.checked)
    }

    @Test
    fun dismissedVersionStaysSilent() {
        val result = AppLaunchPrompts.decide(
            lastSeenVersion = "0.1.0",
            currentVersion = "0.1.0",
            lastCheckAt = null,
            dismissedVersion = "0.2.0",
            now = 0L,
            checksEnabled = true,
            release = GithubRelease.Parsed(
                htmlUrl = DonateLinks.RELEASES_PAGE,
                assets = listOf(
                    ProductUpdate.NamedAsset("aetherfeed-0.2.0-foss.apk", "https://ex/app.apk"),
                ),
            ),
        )
        assertNull(result.prompt)
    }
}

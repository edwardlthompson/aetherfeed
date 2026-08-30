package org.aetherfeed.app.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductUpdateTest {
    @Test
    fun dailyCheckWaitsAFullDay() {
        assertTrue(ProductUpdate.shouldCheckDaily(null, 0L))
        assertFalse(ProductUpdate.shouldCheckDaily(0L, ProductUpdate.MS_DAY - 1))
        assertTrue(ProductUpdate.shouldCheckDaily(0L, ProductUpdate.MS_DAY))
    }

    @Test
    fun assetVersionsComeFromFilenamesNotTags() {
        assertEquals(
            "1.10.8",
            ProductUpdate.parseApkVersion("aetherfeed-1.10.8-foss.apk"),
        )
        assertEquals(
            "0.17.4",
            ProductUpdate.parseExeVersion("AetherFeed-0.17.4-x64-setup.exe"),
        )
        assertNull(ProductUpdate.parseApkVersion("v0.22.1"))
        assertNull(ProductUpdate.parseExeVersion("v0.22.1"))
    }

    @Test
    fun selectsMatchingApkUrl() {
        val picked = ProductUpdate.selectApkAsset(
            listOf(
                ProductUpdate.NamedAsset("sbom.cyclonedx.json", "https://example.com/sbom"),
                ProductUpdate.NamedAsset(
                    "aetherfeed-0.18.0-foss.apk",
                    "https://example.com/app.apk",
                ),
            ),
        )
        assertEquals(ProductUpdate.ProductAsset("0.18.0", "https://example.com/app.apk"), picked)
    }

    @Test
    fun donateNudgeOnlyAfterVersionChange() {
        assertFalse(ProductUpdate.shouldNudgeDonate(null, "0.1.0"))
        assertFalse(ProductUpdate.shouldNudgeDonate("0.1.0", "0.1.0"))
        assertTrue(ProductUpdate.shouldNudgeDonate("0.1.0", "0.2.0"))
    }

    @Test
    fun updatePromptSkipsDismissedOrEqualVersions() {
        assertTrue(ProductUpdate.isNewerVersion("0.1.0", "0.2.0"))
        assertTrue(ProductUpdate.shouldPromptUpdate("0.1.0", "0.2.0", null))
        assertFalse(ProductUpdate.shouldPromptUpdate("0.1.0", "0.2.0", "0.2.0"))
        assertFalse(ProductUpdate.shouldPromptUpdate("0.2.0", "0.2.0", null))
        assertFalse(ProductUpdate.shouldPromptUpdate("0.1.0", null, null))
    }
}

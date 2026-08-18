package org.aetherfeed.app.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class DonationsLoaderTest {
    @Test
    fun parsesEnabledDonations() {
        val cfg = DonationsLoader.parse(
            """
            {
              "enabled": true,
              "message": "If this project helps you, consider supporting development.",
              "links": [{ "label": "[INSERT METHOD]", "url": "https://example.invalid" }]
            }
            """.trimIndent(),
        )
        assertTrue(cfg.enabled)
        assertEquals("If this project helps you, consider supporting development.", cfg.message)
        assertEquals(1, cfg.links.size)
        assertEquals("[INSERT METHOD]", cfg.links[0].label)
    }

    @Test
    fun parsesDisabledWhenEmptyObject() {
        val cfg = DonationsLoader.parse("{}")
        assertFalse(cfg.enabled)
        assertEquals("", cfg.message)
        assertTrue(cfg.links.isEmpty())
    }
}

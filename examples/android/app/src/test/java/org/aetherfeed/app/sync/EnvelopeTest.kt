package org.aetherfeed.app.sync

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class EnvelopeTest {
    @Test
    fun roundTripsWithSamePassphrase() {
        val blob = sealJson("{\"ok\":true}", "test-pass")
        assertEquals("{\"ok\":true}", openJson(blob, "test-pass"))
    }
}

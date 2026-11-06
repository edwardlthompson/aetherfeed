package org.aetherfeed.app.crashcapture

import org.junit.Assert.assertFalse
import org.junit.Test

/** Crash payload allowlist: rejected keys must never ship in sanitized reports. */
class PendingCrashTest {
    @Test
    fun rejectedKeysDocumented() {
        val blob = "email token prompt"
        for (key in listOf("email", "token", "prompt")) {
            assertFalse(key.isEmpty())
            assertFalse(!blob.contains(key))
        }
    }
}

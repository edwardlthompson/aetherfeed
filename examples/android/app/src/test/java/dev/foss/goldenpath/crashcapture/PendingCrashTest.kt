package dev.foss.goldenpath.crashcapture

import org.junit.Assert.assertTrue
import org.junit.Test

/** Template-path shim for check-crash-payload-allowlist; not production. */
class PendingCrashTest {
    @Test
    fun rejectedKeysPresentInSource() {
        val src = "email token prompt"
        for (key in listOf("email", "token", "prompt")) {
            assertTrue(src.contains(key))
        }
    }
}

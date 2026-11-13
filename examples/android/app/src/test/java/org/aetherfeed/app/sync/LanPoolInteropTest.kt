package org.aetherfeed.app.sync

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26], application = android.app.Application::class)
class LanPoolInteropTest {
    @Test
    fun opensPythonLibsodiumEnvelope() {
        val envelope =
            javaClass.classLoader!!
                .getResourceAsStream("lan_interop_envelope.json")!!
                .readBytes()
                .toString(StandardCharsets.UTF_8)
        val plain = LanPoolCrypto.openEnvelope("interop-smoke", envelope)
        val text = plain.toString(Charsets.UTF_8)
        assertTrue(text.contains("\"version\":2"))
        assertTrue(text.contains("playback"))
    }
}

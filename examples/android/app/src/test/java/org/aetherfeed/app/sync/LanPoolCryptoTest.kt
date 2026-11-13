package org.aetherfeed.app.sync

import android.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26], application = android.app.Application::class)
class LanPoolCryptoTest {
    @Test
    fun roundTripsEnvelope() {
        val plain = """{"version":2,"subs":[]}""".toByteArray(Charsets.UTF_8)
        val sealed = LanPoolCrypto.sealEnvelope("home-wifi-secret", plain)
        val opened = LanPoolCrypto.openEnvelope("home-wifi-secret", sealed)
        assertArrayEquals(plain, opened)
    }

    @Test
    fun rejectsEmptyPassphrase() {
        assertThrows(IllegalArgumentException::class.java) {
            LanPoolCrypto.sealEnvelope("", byteArrayOf(1))
        }
    }

    @Test
    fun putBearerStable() {
        val salt = Base64.encodeToString(ByteArray(16) { 1 }, Base64.NO_WRAP)
        val a = LanPoolCrypto.putBearer("secret", salt, "etag-1")
        val b = LanPoolCrypto.putBearer("secret", salt, "etag-1")
        assertEquals(a, b)
    }
}

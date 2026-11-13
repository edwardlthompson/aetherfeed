package org.aetherfeed.app.sync

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FakeDriveSyncProviderTest {
    @Test
    fun pushThenPull_roundTrip_inMemory() = runTest {
        val provider = FakeDriveSyncProvider()
        val blob = byteArrayOf(1, 2, 3, 4)
        assertNull(provider.pull())
        provider.push(blob)
        assertArrayEquals(blob, provider.pull())
        provider.push(byteArrayOf(9))
        assertArrayEquals(byteArrayOf(9), provider.pull())
    }
}

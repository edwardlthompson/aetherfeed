package org.aetherfeed.app.sync

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WebDavProviderTest {
    @Test
    fun pushThenPull_roundTrip() = runTest {
        val store = mutableMapOf<String, ByteArray>()
        val http =
            RecordingSyncHttpClient { method, url, _, body ->
                when (method) {
                    "GET" ->
                        SyncHttpResponse(
                            if (store.containsKey(url)) 200 else 404,
                            store[url]?.copyOf() ?: ByteArray(0),
                        )
                    "PUT" -> {
                        val payload = body ?: ByteArray(0)
                        store[url] = payload
                        SyncHttpResponse(201, ByteArray(0))
                    }
                    else -> SyncHttpResponse(405, ByteArray(0))
                }
            }
        val provider = WebDavProvider(http, "https://dav.example.test/sync")
        val blob = byteArrayOf(10, 11, 12)
        assertNull(provider.pull())
        provider.push(blob)
        assertArrayEquals(blob, provider.pull())
    }
}

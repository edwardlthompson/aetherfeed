package org.aetherfeed.app.sync

import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class DriveAppDataProviderTest {
    @Test
    fun pull_returnsNullWhenNoRemoteFile() = runTest {
        val http =
            RecordingSyncHttpClient { method, url, _, _ ->
                when {
                    method == "GET" && url.contains("spaces=appDataFolder") ->
                        SyncHttpResponse(200, """{"files":[]}""".toByteArray())
                    else -> SyncHttpResponse(404, ByteArray(0))
                }
            }
        val provider = DriveAppDataProvider(http, "test-token")
        assertNull(provider.pull())
    }

    @Test
    fun pushThenPull_roundTripViaFakeHttp() = runTest {
        val blobs = mutableMapOf<String, ByteArray>()
        val fileId = "file-1"
        var listed = false
        val http =
            RecordingSyncHttpClient { method, url, _, body ->
                when {
                    method == "GET" && url.contains("spaces=appDataFolder") ->
                        if (!listed) {
                            SyncHttpResponse(200, """{"files":[]}""".toByteArray())
                        } else {
                            SyncHttpResponse(
                                200,
                                JSONObject()
                                    .put(
                                        "files",
                                        org.json.JSONArray()
                                            .put(
                                                JSONObject()
                                                    .put("id", fileId)
                                                    .put("name", SYNC_BLOB_NAME),
                                            ),
                                    )
                                    .toString()
                                    .toByteArray(),
                            )
                        }
                    method == "GET" && url.contains("alt=media") ->
                        SyncHttpResponse(200, blobs[fileId] ?: ByteArray(0))
                    method == "PATCH" && url.contains("upload/drive") ->
                        SyncHttpResponse(200, ByteArray(0))
                    method == "PATCH" && url.contains("/drive/v3/files/") ->
                        SyncHttpResponse(200, ByteArray(0))
                    method == "POST" && url.contains("upload/drive") -> {
                        val payload = body ?: ByteArray(0)
                        blobs[fileId] = payload
                        listed = true
                        SyncHttpResponse(
                            200,
                            JSONObject().put("id", fileId).toString().toByteArray(),
                        )
                    }
                    else -> SyncHttpResponse(500, "unexpected $method $url".toByteArray())
                }
            }
        val provider = DriveAppDataProvider(http, "test-token")
        val blob = byteArrayOf(7, 8, 9)
        provider.push(blob)
        assertArrayEquals(blob, provider.pull())
        assertEquals(1, http.requests.count { it.method == "POST" })
    }
}

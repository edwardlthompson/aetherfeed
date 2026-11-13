package org.aetherfeed.app.sync

import org.json.JSONArray
import org.json.JSONObject

/** JSON array helpers for pool document rows. */
internal object PoolJson {
    private val ALLOWED =
        setOf(
            SubscriptionPoolCodec.KIND_NEWS,
            SubscriptionPoolCodec.KIND_PODCAST,
            SubscriptionPoolCodec.KIND_BOORU,
        )

    fun encodeSubs(rows: List<PoolSub>): JSONArray {
        val arr = JSONArray()
        for (sub in rows) {
            arr.put(
                JSONObject()
                    .put("url", sub.url)
                    .put("title", sub.title)
                    .put("kind", sub.kind)
                    .put("tag", sub.tag)
                    .put("updatedAt", sub.updatedAt),
            )
        }
        return arr
    }

    fun encodeReads(rows: List<PoolRead>): JSONArray {
        val arr = JSONArray()
        for (row in rows) {
            arr.put(JSONObject().put("url", row.url).put("read", row.read).put("updatedAt", row.updatedAt))
        }
        return arr
    }

    fun encodeStars(rows: List<PoolStar>): JSONArray {
        val arr = JSONArray()
        for (row in rows) {
            arr.put(
                JSONObject()
                    .put("url", row.url)
                    .put("starred", row.starred)
                    .put("updatedAt", row.updatedAt),
            )
        }
        return arr
    }

    fun encodePlayback(rows: List<PoolPlayback>): JSONArray {
        val arr = JSONArray()
        for (row in rows) {
            arr.put(
                JSONObject()
                    .put("url", row.url)
                    .put("positionMs", row.positionMs)
                    .put("updatedAt", row.updatedAt),
            )
        }
        return arr
    }

    fun decodeSubs(arr: JSONArray): List<PoolSub> =
        buildList {
            for (i in 0 until arr.length()) {
                val row = arr.getJSONObject(i)
                val url = SubscriptionPoolCodec.normalizeUrl(row.optString("url"))
                if (url.isBlank()) {
                    throw PoolCodecError.BlankUrl()
                }
                val kind = row.optString("kind").trim().lowercase()
                if (kind !in ALLOWED) {
                    throw PoolCodecError.InvalidKind("kind=$kind")
                }
                add(
                    PoolSub(
                        url = url,
                        title = row.optString("title"),
                        kind = kind,
                        tag = row.optString("tag"),
                        updatedAt = row.optLong("updatedAt", 0L),
                    ),
                )
            }
        }

    fun decodeReads(arr: JSONArray): List<PoolRead> =
        buildList {
            for (i in 0 until arr.length()) {
                val row = arr.getJSONObject(i)
                val url = SubscriptionPoolCodec.normalizeUrl(row.optString("url"))
                if (url.isBlank()) {
                    throw PoolCodecError.BlankUrl()
                }
                add(
                    PoolRead(
                        url = url,
                        read = row.optBoolean("read", true),
                        updatedAt = row.optLong("updatedAt", 0L),
                    ),
                )
            }
        }

    fun decodeStars(arr: JSONArray): List<PoolStar> =
        buildList {
            for (i in 0 until arr.length()) {
                val row = arr.getJSONObject(i)
                val url = SubscriptionPoolCodec.normalizeUrl(row.optString("url"))
                if (url.isBlank()) {
                    throw PoolCodecError.BlankUrl()
                }
                add(
                    PoolStar(
                        url = url,
                        starred = row.optBoolean("starred", true),
                        updatedAt = row.optLong("updatedAt", 0L),
                    ),
                )
            }
        }

    fun decodePlayback(arr: JSONArray): List<PoolPlayback> =
        buildList {
            for (i in 0 until arr.length()) {
                val row = arr.getJSONObject(i)
                val url = SubscriptionPoolCodec.normalizeUrl(row.optString("url"))
                if (url.isBlank()) {
                    throw PoolCodecError.BlankUrl()
                }
                add(
                    PoolPlayback(
                        url = url,
                        positionMs = row.optLong("positionMs", 0L),
                        updatedAt = row.optLong("updatedAt", 0L),
                    ),
                )
            }
        }
}

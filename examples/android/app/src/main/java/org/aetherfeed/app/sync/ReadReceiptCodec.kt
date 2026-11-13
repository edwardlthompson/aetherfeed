package org.aetherfeed.app.sync

import org.json.JSONArray
import org.json.JSONObject

/** Keep in sync with Hermes sibling `org.hermeslauncher.app.feeds.pool.ReadReceiptCodec`. */
object ReadReceiptCodec {
    const val VERSION = 1

    fun encode(doc: ReadReceiptDocument): String {
        val validated = requireValid(doc)
        val arr = JSONArray()
        for (row in validated.reads) {
            arr.put(
                JSONObject()
                    .put("url", row.url)
                    .put("read", row.read)
                    .put("updatedAt", row.updatedAt),
            )
        }
        return JSONObject()
            .put("version", validated.version)
            .put("updatedAt", validated.updatedAt)
            .put("reads", arr)
            .toString()
    }

    fun decode(raw: String): ReadReceiptDocument {
        if (raw.isBlank()) {
            throw ReadReceiptError.InvalidJson("empty")
        }
        val obj =
            try {
                JSONObject(raw)
            } catch (e: Exception) {
                throw ReadReceiptError.InvalidJson(e.message ?: "parse failed")
            }
        val version = obj.optInt("version", -1)
        if (version != VERSION) {
            throw ReadReceiptError.UnsupportedVersion(version)
        }
        val arr = obj.optJSONArray("reads") ?: JSONArray()
        val reads =
            buildList {
                for (i in 0 until arr.length()) {
                    val row = arr.getJSONObject(i)
                    val url = SubscriptionPoolCodec.normalizeUrl(row.optString("url"))
                    if (url.isBlank()) {
                        throw ReadReceiptError.BlankUrl()
                    }
                    add(
                        ReadReceipt(
                            url = url,
                            read = row.optBoolean("read", true),
                            updatedAt = row.optLong("updatedAt", 0L),
                        ),
                    )
                }
            }
        return requireValid(
            ReadReceiptDocument(version = version, updatedAt = obj.optLong("updatedAt", 0L), reads = reads),
        )
    }

    fun mergeLww(local: List<ReadReceipt>, incoming: List<ReadReceipt>): List<ReadReceipt> {
        val map = LinkedHashMap<String, ReadReceipt>()
        for (row in local) {
            val url = SubscriptionPoolCodec.normalizeUrl(row.url)
            map[url] = row.copy(url = url)
        }
        for (row in incoming) {
            val url = SubscriptionPoolCodec.normalizeUrl(row.url)
            val next = row.copy(url = url)
            val prior = map[url]
            if (prior == null || next.updatedAt > prior.updatedAt) {
                map[url] = next
            }
        }
        return map.values.toList()
    }

    fun requireValid(doc: ReadReceiptDocument): ReadReceiptDocument {
        if (doc.version != VERSION) {
            throw ReadReceiptError.UnsupportedVersion(doc.version)
        }
        val seen = LinkedHashMap<String, ReadReceipt>()
        for (row in doc.reads) {
            val url = SubscriptionPoolCodec.normalizeUrl(row.url)
            if (url.isBlank()) {
                throw ReadReceiptError.BlankUrl()
            }
            seen[url] = row.copy(url = url)
        }
        return doc.copy(reads = seen.values.toList())
    }
}

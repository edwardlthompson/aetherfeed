package org.aetherfeed.app.sync

import org.json.JSONArray
import org.json.JSONObject

/** Keep in sync with Hermes sibling `org.hermeslauncher.app.feeds.pool.SubscriptionPoolCodec`. */
object SubscriptionPoolCodec {
    const val VERSION = 2
    const val KIND_NEWS = "news"
    const val KIND_PODCAST = "podcast"
    const val KIND_BOORU = "booru"
    private val ALLOWED = setOf(KIND_NEWS, KIND_PODCAST, KIND_BOORU)
    private const val SOFT_MAX_BYTES = 1_000_000

    fun isLauncherKind(kind: String): Boolean = kind == KIND_NEWS || kind == KIND_PODCAST

    fun normalizeUrl(url: String): String {
        var t = url.trim()
        while (t.length > 1 && t.endsWith('/')) {
            t = t.dropLast(1)
        }
        return t
    }

    fun normalizeSub(sub: PoolSub): PoolSub =
        sub.copy(
            url = normalizeUrl(sub.url),
            kind = sub.kind.trim().lowercase(),
            title = sub.title.trim(),
            tag = sub.tag.trim(),
        )

    fun encode(doc: PoolDocument): String {
        val validated = requireValid(doc)
        val raw =
            JSONObject()
                .put("version", validated.version)
                .put("updatedAt", validated.updatedAt)
                .put("subs", PoolJson.encodeSubs(validated.subs))
                .put("reads", PoolJson.encodeReads(validated.reads))
                .put("stars", PoolJson.encodeStars(validated.stars))
                .put("playback", PoolJson.encodePlayback(validated.playback))
                .toString()
        check(raw.length < SOFT_MAX_BYTES) { "pool document exceeds soft size" }
        return raw
    }

    fun decode(raw: String): PoolDocument {
        if (raw.isBlank()) {
            throw PoolCodecError.InvalidJson("empty document")
        }
        val obj =
            try {
                JSONObject(raw)
            } catch (e: Exception) {
                throw PoolCodecError.InvalidJson(e.message ?: "parse failed")
            }
        val version = obj.optInt("version", -1)
        if (version != 1 && version != VERSION) {
            throw PoolCodecError.UnsupportedVersion(version)
        }
        return requireValid(
            PoolDocument(
                version = VERSION,
                updatedAt = obj.optLong("updatedAt", 0L),
                subs = PoolJson.decodeSubs(obj.optJSONArray("subs") ?: JSONArray()),
                reads = PoolJson.decodeReads(obj.optJSONArray("reads") ?: JSONArray()),
                stars = PoolJson.decodeStars(obj.optJSONArray("stars") ?: JSONArray()),
                playback = PoolJson.decodePlayback(obj.optJSONArray("playback") ?: JSONArray()),
            ),
        )
    }

    fun mergeLww(local: List<PoolSub>, incoming: List<PoolSub>): List<PoolSub> =
        PoolMerge.mergeSubs(local, incoming)

    fun requireValid(doc: PoolDocument): PoolDocument {
        if (doc.version != VERSION) {
            throw PoolCodecError.UnsupportedVersion(doc.version)
        }
        val seen = LinkedHashMap<String, PoolSub>()
        for (sub in doc.subs) {
            val n = normalizeSub(sub)
            if (n.url.isBlank()) {
                throw PoolCodecError.BlankUrl()
            }
            if (n.kind !in ALLOWED) {
                throw PoolCodecError.InvalidKind("kind=${n.kind}")
            }
            seen[n.url] = n
        }
        return doc.copy(
            version = VERSION,
            subs = seen.values.toList(),
            reads = dedupeReads(doc.reads),
            stars = dedupeStars(doc.stars),
            playback = dedupePlayback(doc.playback),
        )
    }

    private fun dedupeReads(rows: List<PoolRead>): List<PoolRead> {
        val seen = LinkedHashMap<String, PoolRead>()
        for (row in rows) {
            val url = normalizeUrl(row.url)
            if (url.isBlank()) {
                throw PoolCodecError.BlankUrl()
            }
            seen[url] = row.copy(url = url)
        }
        return seen.values.toList()
    }

    private fun dedupeStars(rows: List<PoolStar>): List<PoolStar> {
        val seen = LinkedHashMap<String, PoolStar>()
        for (row in rows) {
            val url = normalizeUrl(row.url)
            if (url.isBlank()) {
                throw PoolCodecError.BlankUrl()
            }
            seen[url] = row.copy(url = url)
        }
        return seen.values.toList()
    }

    private fun dedupePlayback(rows: List<PoolPlayback>): List<PoolPlayback> {
        val seen = LinkedHashMap<String, PoolPlayback>()
        for (row in rows) {
            val url = normalizeUrl(row.url)
            if (url.isBlank()) {
                throw PoolCodecError.BlankUrl()
            }
            seen[url] = row.copy(url = url)
        }
        return seen.values.toList()
    }
}

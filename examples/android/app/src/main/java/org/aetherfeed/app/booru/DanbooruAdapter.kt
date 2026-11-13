package org.aetherfeed.app.booru

import org.aetherfeed.app.domain.BooruPost
import org.json.JSONArray
import org.json.JSONObject

class DanbooruAdapter : BooruAdapter {
    override fun mapPosts(rawJson: String, sourceId: String): List<BooruPost> {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return emptyList()
        val array =
            when {
                trimmed.startsWith("[") -> JSONArray(trimmed)
                trimmed.startsWith("{") -> {
                    val obj = JSONObject(trimmed)
                    when {
                        obj.has("posts") -> obj.getJSONArray("posts")
                        else -> JSONArray().put(obj)
                    }
                }
                else -> return emptyList()
            }
        return buildList(array.length()) {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                mapOne(item, sourceId)?.let(::add)
            }
        }
    }

    fun mapPosts(bytes: ByteArray, sourceId: String): List<BooruPost> =
        mapPosts(bytes.decodeToString(), sourceId)

    companion object {
        fun searchUrl(baseUrl: String, query: BooruSearchQuery): String {
            val root = baseUrl.trimEnd('/')
            val tagParam =
                query.tags
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString("+")
            val params =
                buildList {
                    if (tagParam.isNotEmpty()) add("tags=$tagParam")
                    add("page=${query.page}")
                    add("limit=${query.limit}")
                }
            return "$root/posts.json?${params.joinToString("&")}"
        }

        private fun mapOne(item: JSONObject, sourceId: String): BooruPost? {
            val remoteId = item.optLong("id", -1L).takeIf { it >= 0 }?.toString()
                ?: item.optString("id").takeIf { it.isNotBlank() }
                ?: return null
            val fileUrl =
                item.optString("file_url").takeIf { it.isNotBlank() }
                    ?: item.optString("large_file_url").takeIf { it.isNotBlank() }
                    ?: return null
            val preview =
                item.optString("preview_file_url").takeIf { it.isNotBlank() }
                    ?: item.optString("preview_url").takeIf { it.isNotBlank() }
            val tags =
                item.optString("tag_string")
                    .ifBlank { item.optString("tags") }
                    .split(' ')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            return BooruPost(
                id = "$sourceId:$remoteId",
                sourceId = sourceId,
                remoteId = remoteId,
                fileUrl = fileUrl,
                previewUrl = preview,
                tags = tags,
            )
        }
    }
}

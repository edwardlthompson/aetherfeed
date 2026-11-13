package org.aetherfeed.app.export

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.PlaybackPosition
import org.aetherfeed.app.domain.Star
import org.json.JSONArray
import org.json.JSONObject

const val LIBRARY_EXPORT_VERSION = 1

data class LibraryExportSnapshot(
    val version: Int = LIBRARY_EXPORT_VERSION,
    val exportedAt: Long,
    val feeds: List<Feed>,
    val stars: List<Star>,
    val playback: List<PlaybackPosition>,
) {
    init {
        require(version == LIBRARY_EXPORT_VERSION)
    }
}

fun exportLibraryJson(snapshot: LibraryExportSnapshot): ByteArray =
    JSONObject()
        .put("version", snapshot.version)
        .put("exportedAt", snapshot.exportedAt)
        .put("feeds", feedsToJson(snapshot.feeds))
        .put("stars", starsToJson(snapshot.stars))
        .put("playback", playbackToJson(snapshot.playback))
        .toString(2)
        .toByteArray(Charsets.UTF_8)

private fun feedsToJson(feeds: List<Feed>): JSONArray =
    JSONArray().also { arr ->
        for (feed in feeds) {
            arr.put(
                JSONObject()
                    .put("id", feed.id)
                    .put("title", feed.title)
                    .put("url", feed.url)
                    .put("kind", feed.kind.name.lowercase())
                    .put("siteUrl", feed.siteUrl)
                    .put("updatedAt", feed.updatedAt),
            )
        }
    }

private fun starsToJson(stars: List<Star>): JSONArray =
    JSONArray().also { arr ->
        for (star in stars) {
            arr.put(
                JSONObject()
                    .put("targetId", star.targetId)
                    .put("module", star.module.name.lowercase())
                    .put("createdAt", star.createdAt),
            )
        }
    }

private fun playbackToJson(playback: List<PlaybackPosition>): JSONArray =
    JSONArray().also { arr ->
        for (pos in playback) {
            arr.put(
                JSONObject()
                    .put("episodeId", pos.episodeId)
                    .put("positionMs", pos.positionMs)
                    .put("durationMs", pos.durationMs)
                    .put("updatedAt", pos.updatedAt),
            )
        }
    }

fun moduleKindFromExport(value: String): ModuleKind =
    when (value.lowercase()) {
        "news" -> ModuleKind.News
        "podcast" -> ModuleKind.Podcast
        "booru" -> ModuleKind.Booru
        else -> error("unknown module: $value")
    }

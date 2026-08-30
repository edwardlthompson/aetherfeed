package org.aetherfeed.app.readerimport

import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus
import org.aetherfeed.app.domain.Star
import org.aetherfeed.app.news.flattenOpml

fun feedIdFromUrl(url: String): String = "feed:$url"

private fun readStatusOf(value: String): ReadStatus = when (value) {
    "read" -> ReadStatus.Read
    "in_progress" -> ReadStatus.InProgress
    else -> ReadStatus.Unread
}

data class ReaderImportPlan(
    val feeds: List<Feed>,
    val stars: List<Star>,
    val reads: List<ReadState>,
    val result: ReaderImportResult,
)

fun planReaderImport(
    existingUrls: Set<String>,
    parsed: ReaderImportParsed,
    now: Long,
): ReaderImportPlan {
    val errors = parsed.errors.toMutableList()
    if (isHardParseFailure(parsed)) {
        return ReaderImportPlan(
            emptyList(), emptyList(), emptyList(),
            ReaderImportResult(
                parsed.vendor, 0, 0, 0,
                errors.ifEmpty { listOf(ReaderImportError(ReaderImportErrorCode.Malformed, "No subscriptions found")) },
                0, 0,
            ),
        )
    }
    val seen = existingUrls.toMutableSet()
    val feeds = mutableListOf<Feed>()
    var skipped = 0
    for (outline in flattenOpml(parsed.outlines)) {
        val url = outline.xmlUrl?.trim().orEmpty()
        if (url.isEmpty()) {
            errors.add(ReaderImportError(ReaderImportErrorCode.Skipped, "Outline missing xmlUrl", outline.title))
            continue
        }
        val key = normalizeFeedUrl(url)
        if (key in seen) {
            skipped += 1
            continue
        }
        seen.add(key)
        val kind = classifyFeedKind(outline)
        feeds.add(
            Feed(
                id = feedIdFromUrl(url),
                title = outline.title.ifBlank { url },
                url = url,
                kind = kind,
                siteUrl = outline.htmlUrl,
                updatedAt = now,
                folder = outline.folder?.trim()?.ifBlank { null },
            ),
        )
    }
    val stars = parsed.stars.map { Star(it.url, ModuleKind.News, now) }
    val reads = parsed.reads.map { ReadState(it.url, ModuleKind.News, readStatusOf(it.status), now) }
    return ReaderImportPlan(
        feeds, stars, reads,
        ReaderImportResult(
            parsed.vendor, feeds.size, skipped, stars.size, errors,
            feeds.count { it.kind == ModuleKind.News },
            feeds.count { it.kind == ModuleKind.Podcast },
        ),
    )
}

class ReaderImportRepository(private val library: LibraryRepository) {
    suspend fun apply(parsed: ReaderImportParsed, now: Long = System.currentTimeMillis()): ReaderImportResult {
        val existing = library.feeds().map { normalizeFeedUrl(it.url) }.toSet()
        val planned = planReaderImport(existing, parsed, now)
        for (feed in planned.feeds) library.upsertFeed(feed)
        for (star in planned.stars) library.upsertStar(star)
        for (read in planned.reads) library.upsertReadState(read)
        return planned.result
    }
}

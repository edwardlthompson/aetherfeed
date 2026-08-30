package org.aetherfeed.app.readerimport

import android.content.Context
import java.io.File
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository

const val SEED_LIBRARY_NAME = "seed-library.json"

private val SMOKE_URLS = setOf(
    "https://hnrss.org/frontpage",
    "https://feeds.npr.org/1001/rss.xml",
)
private val SMOKE_KEYS = SMOKE_URLS.map(::normalizeFeedUrl).toSet()

fun isLockEnvelope(text: String): Boolean =
    text.contains("ciphertextB64") && text.contains("\"aead\"")

fun isSmokeDemo(feed: Feed): Boolean {
    if (feed.folder.equals("Smoke", ignoreCase = true)) return true
    val url = normalizeFeedUrl(feed.url)
    if (url !in SMOKE_KEYS) return false
    return feed.title.equals("Hacker News", ignoreCase = true) ||
        feed.title.equals("NPR News", ignoreCase = true)
}

fun firstLiveFeed(feeds: List<Feed>): Feed? =
    feeds.firstOrNull { !isSmokeDemo(it) } ?: feeds.firstOrNull()

suspend fun applySeedLibrary(context: Context, library: LibraryRepository): ReaderImportResult? {
    val file = File(context.filesDir, SEED_LIBRARY_NAME)
    if (!file.isFile) return null
    val text = file.readText()
    if (isLockEnvelope(text)) return null
    val parsed = parseReaderImport(ReaderImportFile(file.name, text))
    val result = ReaderImportRepository(library).apply(parsed)
    val seedUrls = parsed.outlines.mapNotNull { it.xmlUrl?.let(::normalizeFeedUrl) }.toSet()
    pruneSmokeLibrary(library, seedUrls)
    return result
}

suspend fun pruneSmokeLibrary(library: LibraryRepository, keepUrls: Set<String> = emptySet()) {
    for (feed in library.feeds()) {
        if (normalizeFeedUrl(feed.url) in keepUrls) continue
        if (isSmokeDemo(feed)) library.deleteFeed(feed.id)
    }
}

suspend fun dropSmokeNotInSeed(library: LibraryRepository, seedUrls: Set<String>) =
    pruneSmokeLibrary(library, seedUrls)

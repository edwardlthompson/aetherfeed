package org.aetherfeed.app.readerimport

import org.aetherfeed.app.news.OpmlOutline

enum class ReaderVendor { GoogleReader, Inoreader, Opml, Unknown }

enum class ReaderImportErrorCode { Empty, Malformed, Skipped }

data class ReaderImportError(
    val code: ReaderImportErrorCode,
    val message: String,
    val outline: String? = null,
)

data class ReaderImportFile(
    val name: String,
    val text: String? = null,
    val members: Map<String, String> = emptyMap(),
)

data class ReaderImportStar(val url: String, val title: String? = null)

data class ReaderImportRead(val url: String, val status: String)

data class ReaderImportParsed(
    val vendor: ReaderVendor,
    val outlines: List<OpmlOutline>,
    val stars: List<ReaderImportStar>,
    val reads: List<ReaderImportRead>,
    val errors: List<ReaderImportError>,
)

data class ReaderImportResult(
    val vendor: ReaderVendor,
    val feedsAdded: Int,
    val feedsSkipped: Int,
    val starsApplied: Int,
    val errors: List<ReaderImportError>,
    val newsAdded: Int = 0,
    val podcastsAdded: Int = 0,
)

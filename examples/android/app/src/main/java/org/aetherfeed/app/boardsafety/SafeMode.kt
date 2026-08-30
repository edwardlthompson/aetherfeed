package org.aetherfeed.app.boardsafety

enum class PostRating { Safe, Questionable, Explicit, Unknown }

data class SafeModeSettings(
    val hideQuestionable: Boolean = true,
    val hideExplicit: Boolean = true,
    val blacklist: Set<String> = emptySet(),
)

fun parseRating(raw: String?): PostRating {
    val key = raw?.trim()?.lowercase().orEmpty()
    return when (key) {
        "s", "safe", "g", "general" -> PostRating.Safe
        "q", "questionable", "sensitive" -> PostRating.Questionable
        "e", "explicit" -> PostRating.Explicit
        else -> PostRating.Unknown
    }
}

fun ratingFromTags(tags: List<String>): PostRating {
    val hit = tags.firstOrNull { it.startsWith("rating:", ignoreCase = true) }
    return parseRating(hit?.substringAfter(":"))
}

fun isBlacklisted(tags: List<String>, blacklist: Set<String>): Boolean {
    val blocked = blacklist.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
    return tags.any { it.trim().lowercase() in blocked }
}

fun allowPost(tags: List<String>, ratingRaw: String?, settings: SafeModeSettings): Boolean {
    if (isBlacklisted(tags, settings.blacklist)) return false
    return when (parseRating(ratingRaw).takeIf { it != PostRating.Unknown } ?: ratingFromTags(tags)) {
        PostRating.Questionable -> !settings.hideQuestionable
        PostRating.Explicit -> !settings.hideExplicit
        PostRating.Safe, PostRating.Unknown -> true
    }
}

package org.aetherfeed.app.news

const val SNIPPET_MAX = 160

enum class AgeUnit { Empty, Now, Hours, Days }

data class AgeLabel(val unit: AgeUnit, val count: Int = 0)

fun plainSnippet(raw: String?, maxChars: Int = SNIPPET_MAX): String {
    val text = raw.orEmpty()
        .replace(Regex("<[^>]+>"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
    if (text.length <= maxChars) return text
    return text.take(maxChars).trimEnd() + "…"
}

fun listSnippet(raw: String?, maxChars: Int = SNIPPET_MAX): String {
    if (isStubBody(raw)) return ""
    val text = plainSnippet(raw, maxChars)
    return if (Regex("https?://").containsMatchIn(text)) "" else text
}

fun ageLabel(publishedAt: Long?, now: Long = System.currentTimeMillis()): AgeLabel {
    if (publishedAt == null) return AgeLabel(AgeUnit.Empty)
    val minutes = ((now - publishedAt).coerceAtLeast(0L)) / 60_000L
    if (minutes < 60) return AgeLabel(AgeUnit.Now)
    val hours = (minutes / 60).toInt()
    if (hours < 48) return AgeLabel(AgeUnit.Hours, hours)
    return AgeLabel(AgeUnit.Days, hours / 24)
}

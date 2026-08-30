package org.aetherfeed.app.podcasts

fun enclosureFromRss(body: String): String {
    if (body.isBlank()) return ""
    val match = Regex("""<enclosure[^>]+url=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(body)
    return match?.groupValues?.getOrNull(1)?.trim().orEmpty()
}

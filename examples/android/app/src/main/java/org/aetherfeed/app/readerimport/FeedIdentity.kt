package org.aetherfeed.app.readerimport

import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.news.OpmlOutline
import java.net.URI

private val podcastHosts = listOf(
    "libsyn.com",
    "megaphone.fm",
    "buzzsprout.com",
    "spreaker.com",
    "anchor.fm",
    "podbean.com",
    "simplecast.com",
    "transistor.fm",
    "captivate.fm",
    "omny.fm",
    "podcasts.apple.com",
    "pinecast.com",
    "art19.com",
)

fun stripFeedPrefix(url: String): String {
    val trimmed = url.trim()
    return if (trimmed.startsWith("feed/", ignoreCase = true)) trimmed.substring(5) else trimmed
}

fun normalizeFeedUrl(url: String): String {
    val raw = stripFeedPrefix(url)
    return runCatching {
        val withScheme = if ("://" in raw) raw else "https://$raw"
        val parsed = URI(withScheme)
        var host = (parsed.host ?: "").lowercase()
        if (host.startsWith("www.")) host = host.removePrefix("www.")
        val path = (parsed.path ?: "").trimEnd('/')
        val protocol = when (parsed.scheme?.lowercase()) {
            "http", "https" -> "https"
            else -> parsed.scheme ?: "https"
        }
        "$protocol://$host$path".lowercase()
    }.getOrElse { raw.lowercase().trimEnd('/') }
}

private fun hostOf(url: String): String? = runCatching {
    val raw = stripFeedPrefix(url)
    val withScheme = if ("://" in raw) raw else "https://$raw"
    var host = URI(withScheme).host?.lowercase() ?: return@runCatching null
    if (host.startsWith("www.")) host = host.removePrefix("www.")
    host
}.getOrNull()

fun classifyFeedKind(outline: OpmlOutline): ModuleKind {
    if (outline.type.equals("podcast", ignoreCase = true)) return ModuleKind.Podcast
    val folder = outline.folder.orEmpty()
    if (folder.contains("podcast", ignoreCase = true) || folder.contains("audio", ignoreCase = true)) {
        return ModuleKind.Podcast
    }
    if (outline.title.contains("podcast", ignoreCase = true)) return ModuleKind.Podcast
    val url = outline.xmlUrl.orEmpty()
    val host = hostOf(url)
    if (host != null && podcastHosts.any { host == it || host.endsWith(".$it") }) return ModuleKind.Podcast
    val path = runCatching {
        val raw = stripFeedPrefix(url)
        URI(if ("://" in raw) raw else "https://$raw").path.orEmpty()
    }.getOrDefault("")
    if (path.contains("/podcast", ignoreCase = true)) return ModuleKind.Podcast
    return ModuleKind.News
}

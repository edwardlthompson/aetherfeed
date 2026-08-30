package org.aetherfeed.app.news

const val IMAGE_TIMEOUT_MS = 8_000L
const val IMAGE_MAX_COUNT = 12
const val IMAGE_MAX_BYTES = 1_500_000

fun lastSrcsetUrl(srcset: String): String? =
    srcset.split(",").map { it.trim().substringBefore(" ").trim() }.lastOrNull(::isFetchableImageSrc)

fun promoteLazyImages(html: String): String {
    return html.replace(Regex("(?is)<img\\b[^>]*>")) { match ->
        val tag = match.value
        val src = Regex("""(?i)\bsrc=["']([^"']+)["']""").find(tag)?.groupValues?.getOrNull(1).orEmpty()
        if (isFetchableImageSrc(src)) return@replace tag
        val lazy = listOf("data-src", "data-lazy-src", "data-original").firstNotNullOfOrNull { name ->
            Regex("""(?i)\b$name=["']([^"']+)["']""").find(tag)?.groupValues?.getOrNull(1)
        } ?: Regex("""(?i)\bsrcset=["']([^"']+)["']""").find(tag)?.groupValues?.getOrNull(1)?.let(::lastSrcsetUrl)
        val href = lazy?.trim().orEmpty()
        if (!isFetchableImageSrc(href)) tag
        else if (Regex("""(?i)\bsrc=""").containsMatchIn(tag)) {
            tag.replace(Regex("""(?i)\bsrc=["'][^"']*["']"""), """src="$href"""")
        } else {
            tag.replace(Regex("""(?i)<img\b"""), """<img src="$href"""")
        }
    }
}

fun isFetchableImageSrc(src: String): Boolean {
    val lower = src.trim().lowercase()
    if (lower.isEmpty()) return false
    if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("blob:")) {
        return false
    }
    return lower.startsWith("https://") || lower.startsWith("http://")
}

fun collectImageSrcs(html: String): List<String> {
    val found = Regex("""(?is)<img\b[^>]*>""").findAll(html)
    val srcs = found.mapNotNull { match ->
        val tag = match.value
        val src = Regex("""(?i)src=["']([^"']+)["']""").find(tag)?.groupValues?.getOrNull(1) ?: return@mapNotNull null
        val width = Regex("""(?i)\bwidth=["']?(\d+)""").find(tag)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val height = Regex("""(?i)\bheight=["']?(\d+)""").find(tag)?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (isFetchableImageSrc(src) && isContentImage(src, width, height, tag)) src else null
    }
    return srcs.distinct().take(IMAGE_MAX_COUNT).toList()
}

fun rewriteImageSrcs(html: String, blobs: Map<String, String>): String {
    var next = html
    blobs.forEach { (src, dataUrl) ->
        if (src.isNotBlank() && dataUrl.isNotBlank()) {
            next = next.replace(src, dataUrl)
        }
    }
    return next
}

fun bytesToDataUrl(bytes: ByteArray, mime: String): String {
    val kind = mime.ifBlank { "application/octet-stream" }
    val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    return "data:$kind;base64,$b64"
}

package org.aetherfeed.app.news

private val SKIP = listOf(
    "facebook.com",
    "fbcdn",
    "twitter.com",
    "twimg.com",
    "x.com/intent",
    "linkedin.com",
    "instagram.com",
    "pinterest.",
    "reddit.com",
    "sharethis",
    "addthis",
    "addtoany",
    "/share/",
    "pixel",
    "1x1",
    "tracking",
    "beacon",
    "analytics",
    "doubleclick",
    "googletag",
    "gravatar",
    "favicon",
    "apple-touch",
    "whatsapp",
    "telegram",
    "mastodon",
    "/icon.",
    "/icons/",
    "sprite",
    "badge",
    "social-icon",
    "share-icon",
    "sharer",
)

fun isContentImage(src: String, width: Int? = null, height: Int? = null, hint: String = ""): Boolean {
    val url = src.trim()
    if (url.isEmpty()) return false
    if (width != null && width > 0 && width <= 48) return false
    if (height != null && height > 0 && height <= 48) return false
    val hay = "$url $hint".lowercase()
    return SKIP.none { it in hay }
}

fun dropNonContentImages(html: String): String {
    return html.replace(Regex("(?is)<img\\b[^>]*>")) { match ->
        val tag = match.value
        val src = Regex("""(?i)src=["']([^"']+)["']""").find(tag)?.groupValues?.getOrNull(1).orEmpty()
        val width = Regex("""(?i)\bwidth=["']?(\d+)""").find(tag)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val height = Regex("""(?i)\bheight=["']?(\d+)""").find(tag)?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (isContentImage(src, width, height, tag)) tag else ""
    }
}

fun firstDataThumb(html: String): String? {
    return Regex("""(?i)src=["'](data:image/[^"']+)["']""").find(html)?.groupValues?.getOrNull(1)
}

fun shrinkThumbDataUrl(dataUrl: String, edge: Int = 112): String? {
    val raw = dataUrl.substringAfter("base64,", "")
    if (raw.isBlank()) return null
    return runCatching {
        val bytes = android.util.Base64.decode(raw, android.util.Base64.DEFAULT)
        val full = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@runCatching null
        val width = edge.coerceAtLeast(1)
        val height = (full.height * width.toFloat() / full.width.toFloat()).toInt().coerceAtLeast(1)
        val scaled = android.graphics.Bitmap.createScaledBitmap(full, width, height, true)
        val out = java.io.ByteArrayOutputStream()
        scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, out)
        bytesToDataUrl(out.toByteArray(), "image/jpeg")
    }.getOrNull()
}

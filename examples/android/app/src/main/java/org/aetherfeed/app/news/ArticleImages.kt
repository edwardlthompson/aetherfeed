package org.aetherfeed.app.news

/** Pick article images; skip tracking pixels and social icons. */
object ArticleImages {
    private val imgTag = Regex("""<img\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val srcAttr =
        Regex(
            """(?:src|data-src|data-original|data-lazy-src)\s*=\s*["']([^"']+)["']""",
            RegexOption.IGNORE_CASE,
        )
    private val srcset = Regex("""srcset\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val og =
        Regex(
            """<meta\b[^>]+(?:property|name)\s*=\s*["'](?:og:image|twitter:image)["'][^>]*>""",
            RegexOption.IGNORE_CASE,
        )
    private val content = Regex("""content\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val tinyHint =
        Regex(
            """favicon|sprite|pixel|1x1|tracking|emoji|badge|icon[-_/]|/icons?/|social|share""",
            RegexOption.IGNORE_CASE,
        )

    fun isHttpUrl(url: String): Boolean =
        url.startsWith("https://", ignoreCase = true) || url.startsWith("http://", ignoreCase = true)

    fun looksTinyUrl(url: String): Boolean {
        val lower = url.lowercase()
        return tinyHint.containsMatchIn(lower) || lower.startsWith("data:")
    }

    fun absUrl(raw: String): String? {
        val trimmed = raw.trim()
        val url = if (trimmed.startsWith("//")) "https:$trimmed" else trimmed
        return url.takeIf { isHttpUrl(it) && !looksTinyUrl(it) }
    }

    fun photoKey(url: String): String {
        val path = url.trim().substringBefore('?').substringBefore('#').lowercase()
        return path.replace(Regex("""-\d+x\d+(?=\.[a-z0-9]+$)"""), "")
    }

    fun bestFromImgTag(tag: String): String? {
        var best: String? = null
        var bestW = -1
        val set = srcset.find(tag)?.groupValues?.get(1).orEmpty()
        for (part in set.split(",")) {
            val bits = part.trim().split(Regex("\\s+"))
            val url = absUrl(bits.firstOrNull().orEmpty()) ?: continue
            val w = bits.getOrNull(1)?.removeSuffix("w")?.toIntOrNull() ?: 0
            if (w > bestW || best == null) {
                bestW = w
                best = url
            }
        }
        return best ?: srcAttr.findAll(tag).mapNotNull { absUrl(it.groupValues[1]) }.firstOrNull()
    }

    fun allFromHtml(html: String): List<String> {
        val meta = og.findAll(html).mapNotNull { content.find(it.value)?.groupValues?.get(1) }
        val imgs = imgTag.findAll(html).mapNotNull { bestFromImgTag(it.value) }
        return (meta.mapNotNull { absUrl(it) } + imgs).distinctBy { photoKey(it) }.toList()
    }
}

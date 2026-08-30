package org.aetherfeed.app.news

fun sanitizeReaderHtml(html: String): String {
    var next = html
    listOf(
        "(?is)<script\\b.*?</script>",
        "(?is)<style\\b.*?</style>",
        "(?is)<iframe\\b.*?</iframe>",
        "(?is)<object\\b.*?</object>",
        "(?is)<embed\\b[^>]*>",
        "(?is)<form\\b.*?</form>",
        "(?is)<nav\\b.*?</nav>",
        "(?is)<header\\b.*?</header>",
        "(?is)<footer\\b.*?</footer>",
    ).forEach { next = next.replace(Regex(it), "") }
    next = next.replace(Regex("(?is)</?a\\b[^>]*>"), "")
    next = next.replace(
        Regex("(?is)<(div|section|aside|ol|ul)\\b[^>]*(id|class)=['\"][^'\"]*(comment|disqus|discuss)[^'\"]*['\"][^>]*>.*?</\\1>"),
        "",
    )
    next = next.replace(
        Regex("(?is)<p\\b[^>]*>\\s*(article url|comments url|points|# comments)[\\s\\S]*?</p>"),
        "",
    )
    next = next.replace(
        Regex("""(?i)\s(?:style|class|id|onclick|onerror|onload|href|srcset|target)=("[^"]*"|'[^']*')"""),
        "",
    )
    var stripped = next
    val bareUrl = Regex("(?i)(>[^<]*?)https?://\\S+")
    while (bareUrl.containsMatchIn(stripped)) {
        stripped = stripped.replace(bareUrl, "$1")
    }
    return stripped.trim()
}

fun wrapReaderDocument(body: String, bg: String, fg: String, muted: String): String {
    val safe = if (body.length >= BODY_FLOOR * 8) body else sanitizeReaderHtml(body).ifBlank { "<p></p>" }
    return """
        <!DOCTYPE html><html><head>
        <meta charset="utf-8"/>
        <meta name="viewport" content="width=device-width,initial-scale=1"/>
        <style>
          html,body{margin:0;padding:0;min-height:100%;height:auto;background:$bg;color:$fg;font-family:sans-serif;line-height:1.6;overflow-y:visible;overscroll-behavior:contain;}
          img{max-width:100%;height:auto;}
          a{color:inherit;pointer-events:none;text-decoration:none;}
          *{font-family:inherit !important;}
          figcaption,small{color:$muted;}
        </style></head><body>$safe</body></html>
    """.trimIndent()
}

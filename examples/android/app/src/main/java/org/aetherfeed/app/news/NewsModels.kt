package org.aetherfeed.app.news

enum class FeedFormat { Rss, Atom, JsonFeed }

data class ParsedArticle(
    val id: String,
    val title: String,
    val url: String,
    val publishedAt: Long? = null,
    val summary: String? = null,
    val contentHtml: String? = null,
)

data class ParsedFeed(
    val format: FeedFormat,
    val title: String,
    val siteUrl: String? = null,
    val items: List<ParsedArticle>,
)

data class OpmlOutline(
    val title: String,
    val xmlUrl: String? = null,
    val htmlUrl: String? = null,
    val children: List<OpmlOutline> = emptyList(),
    val folder: String? = null,
    val type: String? = null,
)

fun flattenOpml(outlines: List<OpmlOutline>): List<OpmlOutline> {
    val found = mutableListOf<OpmlOutline>()
    fun walk(nodes: List<OpmlOutline>) {
        for (node in nodes) {
            if (node.xmlUrl != null) found.add(node)
            if (node.children.isNotEmpty()) walk(node.children)
        }
    }
    walk(outlines)
    return found
}

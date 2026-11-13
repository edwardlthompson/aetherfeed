package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed

data class FeedDocument(
    val feeds: List<Feed>,
    val articles: List<Article>,
)

enum class NewsParseCode { Unsupported, Malformed, Empty }

class NewsParseError(
    val code: NewsParseCode,
    message: String,
) : Exception(message)

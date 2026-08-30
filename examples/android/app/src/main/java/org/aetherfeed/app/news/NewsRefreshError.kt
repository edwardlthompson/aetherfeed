package org.aetherfeed.app.news

sealed class NewsRefreshError(message: String) : Exception(message) {
    class Aborted(detail: String = "Refresh aborted") : NewsRefreshError(detail)
    class Timeout(detail: String = "Refresh timed out") : NewsRefreshError(detail)
    class Unavailable(detail: String) : NewsRefreshError(detail)
    class ParseFailed(detail: String) : NewsRefreshError(detail)
    class Gone(detail: String = "Feed gone") : NewsRefreshError(detail)
    class UnknownFeed(feedId: String) : NewsRefreshError("Unknown feed: $feedId")
}

fun httpRefreshError(code: Int): NewsRefreshError =
    if (code == 404 || code == 410) NewsRefreshError.Gone("HTTP $code")
    else NewsRefreshError.Unavailable("HTTP $code")

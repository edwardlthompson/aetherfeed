package org.aetherfeed.app.news

import android.content.Context
import org.aetherfeed.app.R

fun feedNotice(context: Context, error: Throwable, title: String): String = when (error) {
    is NewsRefreshError.Gone -> context.getString(R.string.news_feed_gone, title)
    is NewsRefreshError.Timeout -> context.getString(R.string.news_feed_timeout, title)
    is NewsRefreshError.ParseFailed -> context.getString(R.string.news_feed_parse, title)
    else -> context.getString(R.string.news_feed_unreachable, title)
}

fun sweepNotice(context: Context, sweep: RefreshSweep): String = when {
    sweep.goneTitles.size == 1 -> context.getString(R.string.news_feed_gone, sweep.goneTitles[0])
    sweep.goneTitles.size > 1 -> context.getString(R.string.news_feeds_gone, sweep.goneTitles.size)
    else -> sweep.other
}

fun emptyFeedNotice(context: Context, title: String): String =
    context.getString(R.string.news_feed_empty, title)

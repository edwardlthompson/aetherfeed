package org.aetherfeed.app.news

import android.content.Context
import android.content.Intent
import android.net.Uri

object ArticleOpen {
    fun browserIntent(url: String): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun openBrowser(context: Context, url: String): Boolean {
        if (!ArticleImages.isHttpUrl(url)) return false
        return runCatching {
            context.startActivity(browserIntent(url))
            true
        }.getOrDefault(false)
    }
}

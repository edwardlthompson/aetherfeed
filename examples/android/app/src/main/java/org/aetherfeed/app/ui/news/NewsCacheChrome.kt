package org.aetherfeed.app.ui.news

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.aetherfeed.app.R
import org.aetherfeed.app.news.CacheProgress

data class NewsCacheUi(
    val cache: CacheProgress = CacheProgress(),
    val thumbs: Map<String, String> = emptyMap(),
    val article: CacheProgress = CacheProgress(),
    val cached: Set<String> = emptySet(),
    val htmlRev: Int = 0,
)

fun NewsCacheUi.withProgress(progress: CacheProgress, cachedId: String? = null): NewsCacheUi {
    val extra = progress.thumbId?.let { id -> progress.thumb?.let { id to it } }
    return copy(
        cache = progress,
        thumbs = extra?.let { thumbs + it } ?: thumbs,
        cached = cachedId?.let { cached + it } ?: cached,
    )
}

fun decodeDataThumb(dataUrl: String): androidx.compose.ui.graphics.ImageBitmap? {
    val raw = dataUrl.substringAfter("base64,", "")
    if (raw.isBlank()) return null
    return runCatching {
        val bytes = Base64.decode(raw, Base64.DEFAULT)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val sample = ((bounds.outWidth.coerceAtLeast(1)) / 112).coerceAtLeast(1)
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)?.asImageBitmap()
    }.getOrNull()
}

@Composable
internal fun NewsProgressBar(done: Int, total: Int, label: String, testTag: String, modifier: Modifier = Modifier) {
    if (total <= 0 || done >= total) return
    val fraction = (done.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    Column(modifier.testTag(testTag)) {
        Box(Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.outline)) {
            Box(
                Modifier.fillMaxWidth(fraction).fillMaxHeight().background(MaterialTheme.colorScheme.primary),
            )
        }
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier)
    }
}

@Composable
internal fun NewsCacheBar(progress: CacheProgress, modifier: Modifier = Modifier) {
    NewsProgressBar(
        done = progress.done,
        total = progress.total,
        label = stringResource(R.string.news_cache_progress, progress.done, progress.total),
        testTag = "news-cache-progress",
        modifier = modifier,
    )
}

@Composable
internal fun NewsArticleProgress(progress: CacheProgress, modifier: Modifier = Modifier) {
    NewsProgressBar(
        done = progress.done,
        total = progress.total,
        label = stringResource(R.string.news_article_loading, progress.done, progress.total),
        testTag = "news-article-progress",
        modifier = modifier,
    )
}

@Composable
internal fun NewsThumb(dataUrl: String?, modifier: Modifier = Modifier) {
    if (dataUrl.isNullOrBlank()) return
    val bmp = remember(dataUrl) { decodeDataThumb(dataUrl) } ?: return
    Image(
        bmp,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.size(56.dp).testTag("news-thumb"),
    )
}

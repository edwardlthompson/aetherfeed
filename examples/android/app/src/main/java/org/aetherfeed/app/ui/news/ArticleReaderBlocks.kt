package org.aetherfeed.app.ui.news

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.news.ArticleBlock
import org.aetherfeed.app.news.ArticleExtract
import org.aetherfeed.app.ui.theme.SpacingMd
import org.aetherfeed.app.news.ReaderMode

@Composable
fun ReaderModeBar(
    mode: ReaderMode,
    onMode: (ReaderMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = SpacingMd).testTag("reader-mode-bar"),
        horizontalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        ReaderMode.entries.forEach { entry ->
            FilterChip(
                selected = mode == entry,
                onClick = { onMode(entry) },
                label = { Text(stringResource(modeLabel(entry))) },
                modifier = Modifier.testTag("reader-mode-${entry.name.lowercase()}"),
            )
        }
    }
}

@Composable
fun ArticleReaderBlocks(
    article: Article,
    pageHtml: String?,
    mode: ReaderMode,
    showImages: Boolean,
    bodyScale: Float,
    modifier: Modifier = Modifier,
) {
    val source = readingSource(article, pageHtml, mode)
    val loading = pageHtml == null && source.isBlank() && mode != ReaderMode.WEB
    val blocks =
        remember(source, showImages) {
            ArticleExtract.blocks(source).filter { showImages || it !is ArticleBlock.Image }
        }
    Box(modifier = modifier.fillMaxSize().testTag("article-reader-blocks")) {
        when {
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            blocks.isEmpty() ->
                Text(
                    text = stringResource(R.string.feed_reader_empty),
                    modifier = Modifier.padding(SpacingMd),
                )
            else ->
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(SpacingMd),
                ) {
                    blocks.forEach { block ->
                        when (block) {
                            is ArticleBlock.Text ->
                                Text(
                                    text = block.value,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = (16 * bodyScale).sp,
                                )
                            is ArticleBlock.Image -> RemoteArticleImage(url = block.url)
                        }
                    }
                }
        }
    }
}

@Composable
private fun RemoteArticleImage(url: String) {
    var ready by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(url) {
        ready =
            withContext(Dispatchers.IO) {
                runCatching { URL(url).openStream().use { BitmapFactory.decodeStream(it) } }.getOrNull()
            }
    }
    val bmp = ready ?: return
    Image(
        bitmap = bmp.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier.fillMaxWidth().testTag("article-image"),
    )
}

internal fun modeLabel(mode: ReaderMode): Int =
    when (mode) {
        ReaderMode.READING -> R.string.feed_reader_mode_reading
        ReaderMode.FULL -> R.string.feed_reader_mode_full
        ReaderMode.WEB -> R.string.feed_reader_mode_web
    }

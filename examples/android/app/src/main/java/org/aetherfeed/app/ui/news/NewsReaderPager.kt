package org.aetherfeed.app.ui.news

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.news.NewsArticleSwipe
import org.aetherfeed.app.news.overlayReaderHtml
import org.aetherfeed.app.ui.theme.SpacingMd
import org.aetherfeed.app.ui.theme.SpacingSm

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NewsReaderPager(
    articles: List<Article>,
    selected: Article?,
    feedTitleOf: (Article) -> String,
    onOpen: (Article) -> Unit,
    cache: NewsCacheUi,
) {
    if (articles.isEmpty() || selected == null) {
        Text(stringResource(R.string.news_reader_hint), style = MaterialTheme.typography.bodyMedium)
        return
    }
    cache.htmlRev
    val start = articles.indexOfFirst { it.id == selected.id }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = start, pageCount = { articles.size })
    NewsHighRefresh(pagerState.isScrollInProgress)
    LaunchedEffect(selected.id, articles.size) {
        val index = articles.indexOfFirst { it.id == selected.id }
        if (index >= 0 && pagerState.currentPage != index) pagerState.scrollToPage(index)
    }
    LaunchedEffect(pagerState.settledPage) {
        val next = articles.getOrNull(pagerState.settledPage) ?: return@LaunchedEffect
        if (next.id != selected.id) onOpen(next)
    }
    HorizontalPager(
        state = pagerState,
        userScrollEnabled = true,
        beyondViewportPageCount = NewsArticleSwipe.BEYOND_VIEWPORT,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapPositionalThreshold = NewsArticleSwipe.SNAP_FRACTION,
        ),
        modifier = Modifier.fillMaxHeight(),
    ) { page ->
        val article = articles[page]
        Column(Modifier.fillMaxHeight()) {
            NewsArticleProgress(cache.article)
            Text(article.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = SpacingMd))
            if (feedTitleOf(article).isNotBlank()) {
                Text(
                    text = feedTitleOf(article),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            NewsReaderHtml(
                html = overlayReaderHtml(article, selected),
                modifier = Modifier.weight(1f).fillMaxWidth().padding(top = SpacingSm),
            )
        }
    }
}

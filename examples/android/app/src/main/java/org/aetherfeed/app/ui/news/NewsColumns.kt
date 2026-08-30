package org.aetherfeed.app.ui.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.news.AgeUnit
import org.aetherfeed.app.news.ageLabel
import org.aetherfeed.app.news.listSnippet
import org.aetherfeed.app.ui.theme.SpacingMd
import org.aetherfeed.app.ui.theme.SpacingSm
import org.aetherfeed.app.ui.theme.SpacingXs

@Composable
internal fun NewsArticleRow(
    article: Article,
    feedTitle: String,
    selected: Boolean,
    unread: Boolean,
    starred: Boolean,
    showFeedName: Boolean,
    thumb: String? = null,
    cached: Boolean = false,
    onOpen: () -> Unit,
) {
    val age = ageLabel(article.publishedAt)
    val ageText = when (age.unit) {
        AgeUnit.Empty -> ""
        AgeUnit.Now -> stringResource(R.string.news_age_now)
        AgeUnit.Hours -> stringResource(R.string.news_age_hours, age.count)
        AgeUnit.Days -> stringResource(R.string.news_age_days, age.count)
    }
    val snippet = listSnippet(article.summary ?: article.contentHtml)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (unread) 1f else 0.62f)
            .clickable(onClick = onOpen)
            .padding(vertical = SpacingSm)
            .testTag("news-article"),
        horizontalArrangement = Arrangement.spacedBy(SpacingSm),
    ) {
        NewsThumb(thumb)
        Column(Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SpacingXs)) {
            if (unread) {
                Icon(
                    Icons.Rounded.Circle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(8.dp),
                )
            }
            if (cached) {
                Icon(
                    Icons.Rounded.Circle,
                    contentDescription = stringResource(R.string.news_cached),
                    tint = Color(0xFF3DDC84),
                    modifier = Modifier.size(8.dp).testTag("news-cached"),
                )
            }
            if (showFeedName && feedTitle.isNotBlank()) {
                Text(
                    text = feedTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            if (starred) {
                Icon(Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            if (ageText.isNotBlank()) {
                Text(ageText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            text = article.title,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = SpacingXs),
        )
        if (snippet.isNotBlank()) {
            Text(
                text = snippet,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = SpacingXs),
            )
        }
        }
    }
}

@Composable
internal fun NewsReaderColumn(
    articles: List<Article>,
    selected: Article?,
    starred: Set<String>,
    readIds: Set<String>,
    statusText: String,
    feedTitleOf: (Article) -> String,
    onOpen: (Article) -> Unit,
    onStar: (Article) -> Unit,
    onToggleRead: (Article) -> Unit,
    onBack: (() -> Unit)? = null,
    showList: Boolean = true,
    cache: NewsCacheUi = NewsCacheUi(),
    modifier: Modifier = Modifier,
) {
    val showFeedName = articles.map { it.feedId }.toSet().size > 1
    val listScroll = rememberScrollState()
    if (showList) NewsHighRefresh(listScroll.isScrollInProgress)
    val columnMod = if (showList) {
        modifier.verticalScroll(listScroll)
    } else {
        modifier.fillMaxHeight()
    }
    Column(modifier = columnMod.testTag(if (showList) "news-timeline" else "news-reader")) {
        if (onBack != null && selected != null) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.news_back_headlines)) }
        }
        if (statusText.isNotBlank()) {
            Text(
                statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("news-feed-status"),
            )
        }
        if (showList) NewsCacheBar(cache.cache)
        if (showList) {
            articles.forEach { article ->
                NewsArticleRow(
                    article = article,
                    feedTitle = feedTitleOf(article),
                    selected = article.id == selected?.id,
                    unread = article.id !in readIds,
                    starred = article.id in starred,
                    showFeedName = showFeedName,
                    thumb = cache.thumbs[article.id],
                    cached = article.id in cache.cached,
                    onOpen = { onOpen(article) },
                )
            }
        }
        if (!showList) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                NewsReaderPager(
                    articles, selected, feedTitleOf, onOpen, cache,
                )
            }
        }
    }
}

@Composable
internal fun NewsChromeRow(
    groups: List<Pair<String, List<Feed>>>,
    selectedFolder: String?,
    selectedFeedId: String?,
    articles: List<Article>,
    selectedArticle: Article?,
    starred: Set<String>,
    readIds: Set<String>,
    unreadByFolder: Map<String, Int> = emptyMap(),
    unreadByFeed: Map<String, Int> = emptyMap(),
    statusText: String,
    feedTitleOf: (Article) -> String,
    onFolder: (String) -> Unit,
    onFeed: (Feed) -> Unit,
    onRefresh: () -> Unit,
    onOpen: (Article) -> Unit,
    onStar: (Article) -> Unit,
    onToggleRead: (Article) -> Unit,
    onBack: () -> Unit,
    stacked: Boolean,
    refreshing: Boolean = false,
    cache: NewsCacheUi = NewsCacheUi(),
    modifier: Modifier = Modifier,
    library: LibraryTreeState? = null,
) {
    NewsSplitLayout(
        groups, selectedFolder, selectedFeedId, articles, selectedArticle, starred, readIds,
        unreadByFolder, unreadByFeed,
        statusText, feedTitleOf, onFolder, onFeed, onRefresh, onOpen, onStar, onToggleRead,
        onBack, stacked, refreshing, cache, library, modifier,
    )
}

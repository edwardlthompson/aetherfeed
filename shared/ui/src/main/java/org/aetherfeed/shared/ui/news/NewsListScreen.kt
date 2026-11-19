package org.aetherfeed.shared.ui.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.aetherfeed.shared.domain.Article
import org.aetherfeed.shared.ui.theme.SpacingMd

/** Presentational news list (DESIGN_BRIEF §4). Browse prefs / ReaderSplit stay in app until Sprint 58. */
@Composable
fun NewsListScreen(
    articles: List<Article>,
    starredIds: Set<String>,
    unreadIds: Set<String>,
    emptyLabel: String,
    unreadMarker: String,
    starAddCd: String,
    starRemoveCd: String,
    onArticleOpen: (Article) -> Unit,
    onToggleStar: (Article) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (articles.isEmpty()) {
        Text(
            text = emptyLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.fillMaxSize().testTag("news-empty"),
        )
        return
    }
    LazyColumn(modifier = modifier.fillMaxSize().testTag("news-list")) {
        items(articles, key = { it.id }) { article ->
            val starred = article.id in starredIds
            val unread = article.id in unreadIds
            ListItem(
                modifier =
                    Modifier
                        .testTag("news-article-${article.id}")
                        .clickable { onArticleOpen(article) },
                headlineContent = {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        color =
                            if (unread) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                },
                supportingContent =
                    article.summary?.let { summary ->
                        {
                            Text(text = summary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                        }
                    },
                leadingContent =
                    if (unread) {
                        {
                            Text(
                                text = unreadMarker,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = SpacingMd),
                            )
                        }
                    } else {
                        null
                    },
                trailingContent = {
                    IconButton(
                        onClick = { onToggleStar(article) },
                        modifier = Modifier.testTag("news-star-${article.id}"),
                    ) {
                        Icon(
                            imageVector = if (starred) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = if (starred) starRemoveCd else starAddCd,
                            tint =
                                if (starred) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                },
            )
        }
    }
}

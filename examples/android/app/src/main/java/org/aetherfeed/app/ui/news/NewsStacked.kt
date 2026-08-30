package org.aetherfeed.app.ui.news

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewsStackedPane(
    groups: List<Pair<String, List<Feed>>>,
    selectedFolder: String?,
    selectedFeedId: String?,
    expanded: Set<String>,
    onFolder: (String) -> Unit,
    onFeed: (Feed) -> Unit,
    unreadByFolder: Map<String, Int> = emptyMap(),
    unreadByFeed: Map<String, Int> = emptyMap(),
    articles: List<Article>,
    selectedArticle: Article?,
    starred: Set<String>,
    readIds: Set<String>,
    statusText: String,
    feedTitleOf: (Article) -> String,
    onRefresh: () -> Unit,
    onOpen: (Article) -> Unit,
    onStar: (Article) -> Unit,
    onToggleRead: (Article) -> Unit,
    onBack: () -> Unit,
    onOldestFirst: (Boolean) -> Unit,
    oldestFirst: Boolean,
    refreshing: Boolean,
    cache: NewsCacheUi,
    library: LibraryTreeState? = null,
    onToggle: (String) -> Unit = onFolder,
    modifier: Modifier = Modifier,
) {
    NewsNarrowChrome(
        modifier = modifier,
        drawer = { close ->
            NewsFolderDrawer(
                groups = groups,
                selectedFolder = selectedFolder,
                selectedFeedId = selectedFeedId,
                expanded = expanded,
                onFolder = onFolder,
                onFeed = { feed -> close(); onFeed(feed) },
                unreadByFolder = unreadByFolder,
                unreadByFeed = unreadByFeed,
                library = library,
                onToggle = onToggle,
            )
        },
        content = { open ->
            val pane = @Composable {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = open, modifier = Modifier.testTag("news-sources")) {
                            Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.news_sources))
                        }
                        if (selectedArticle == null) {
                            NewsFilterMenu(oldestFirst, onOldestFirst)
                        } else {
                            TextButton(onClick = onBack) { Text(stringResource(R.string.news_back_headlines)) }
                        }
                    }
                    NewsReaderColumn(
                        articles, selectedArticle, starred, readIds, statusText, feedTitleOf,
                        onOpen, onStar, onToggleRead,
                        showList = selectedArticle == null,
                        cache = cache,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )
                }
            }
            if (selectedArticle == null) {
                PullToRefreshBox(
                    isRefreshing = refreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize().testTag("news-pull-refresh"),
                ) { pane() }
            } else {
                Box(Modifier.fillMaxSize()) { pane() }
            }
        },
    )
}

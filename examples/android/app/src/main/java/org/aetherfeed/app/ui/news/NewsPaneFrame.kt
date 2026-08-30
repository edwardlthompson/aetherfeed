package org.aetherfeed.app.ui.news

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.news.newsListEmpty
import org.aetherfeed.app.ui.theme.SpacingMd

private val NewsWideBreakpoint = 600.dp

@Composable
internal fun NewsPaneFrame(
    feeds: List<Feed>,
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
    refreshing: Boolean,
    cache: NewsCacheUi,
    feedTitleOf: (Article) -> String,
    onFolder: (String) -> Unit,
    onFeed: (Feed) -> Unit,
    onRefresh: () -> Unit,
    onOpen: (Article) -> Unit,
    onStar: (Article) -> Unit,
    onToggleRead: (Article) -> Unit,
    onBack: () -> Unit,
    library: LibraryTreeState? = null,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val stacked = maxWidth < NewsWideBreakpoint
        Column(Modifier.fillMaxSize().then(if (stacked) Modifier else Modifier.padding(SpacingMd))) {
            if (newsListEmpty(feeds)) {
                Text(stringResource(R.string.news_empty), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("news-empty"))
            } else {
                NewsChromeRow(
                    groups, selectedFolder, selectedFeedId, articles, selectedArticle, starred, readIds,
                    unreadByFolder, unreadByFeed,
                    statusText, feedTitleOf, onFolder, onFeed, onRefresh, onOpen, onStar, onToggleRead,
                    onBack, stacked, refreshing, cache, Modifier.weight(1f).fillMaxWidth(),
                    library,
                )
            }
        }
    }
}

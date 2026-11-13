package org.aetherfeed.app.ui.news

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.aetherfeed.app.R
import org.aetherfeed.app.browse.BrowsePrefs
import org.aetherfeed.app.browse.ContentFilter
import org.aetherfeed.app.browse.ModuleBrowseState
import org.aetherfeed.app.browse.SortOrder
import org.aetherfeed.app.browse.TreeNode
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.ui.browse.ReaderSplit
import org.aetherfeed.app.news.ArticleNav
import org.aetherfeed.app.news.ArticleOpen
import org.aetherfeed.app.news.ArticleTarget
import org.aetherfeed.app.news.ReaderMode
import org.aetherfeed.app.news.ReaderSettings
import org.aetherfeed.app.ui.theme.SpacingMd

@Composable
fun NewsScreen(
    articles: List<Article>,
    feeds: List<Feed> = emptyList(),
    starredIds: Set<String>,
    unreadIds: Set<String>,
    onArticleOpen: (Article) -> Unit,
    onToggleStar: (Article) -> Unit,
    onParkUnread: (Article) -> Unit = {},
    onReadingChange: (Boolean) -> Unit = {},
    readerSettings: ReaderSettings = ReaderSettings(),
    isOnline: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val browsePrefs = remember { BrowsePrefs(context) }
    val browse by browsePrefs.moduleState("news").collectAsState(ModuleBrowseState())
    val savedMode by browsePrefs.readerMode.collectAsState(ReaderMode.READING)
    var selectedId by remember { mutableStateOf<String?>(null) }
    var readingOrder by remember { mutableStateOf<List<Article>>(emptyList()) }
    val unreadOnly = browse.unreadOnly
    val nodeId = browse.nodeId
    val sort = browse.sort
    val starredLabel = stringResource(R.string.filter_starred)
    val allLabel = stringResource(R.string.filter_all)
    val unreadByFeed =
        remember(articles, feeds, unreadIds) {
            val map = mutableMapOf<String, Int>()
            for (article in articles) {
                if (article.id !in unreadIds) continue
                val key = feeds.firstOrNull { it.id == article.feedId || it.url == article.feedId }?.url
                    ?: article.feedId
                map[key] = (map[key] ?: 0) + 1
            }
            map
        }
    val tree =
        remember(feeds, starredLabel, allLabel, unreadByFeed, unreadOnly, unreadIds, starredIds) {
            ContentFilter.feedTree(
                feeds = feeds.map { it.url to it.title },
                unreadByFeed = unreadByFeed,
                totalUnread = unreadIds.size,
                starredUnread = starredIds.count { it in unreadIds },
                hideEmpty = unreadOnly,
                allLabel = allLabel,
                starredLabel = starredLabel,
            )
        }
    val visible =
        remember(articles, feeds, unreadOnly, nodeId, sort, unreadIds, starredIds) {
            ContentFilter.sortBy(
                ContentFilter.apply(
                    articles,
                    unreadOnly = unreadOnly,
                    starredOnly = nodeId == "starred",
                    inCategory = { article ->
                        val feedUrl =
                            feeds.firstOrNull { it.id == article.feedId || it.url == article.feedId }?.url
                                ?: article.feedId
                        ContentFilter.matchesFolder(nodeId, feedUrl, article.feedId)
                    },
                    isUnread = { unreadIds.contains(it.id) },
                    isStarred = { starredIds.contains(it.id) },
                ),
                sort,
            ) { it.publishedAt ?: 0L }
        }
    val navList = if (selectedId != null && readingOrder.isNotEmpty()) readingOrder else visible
    val selectedIndex = navList.indexOfFirst { it.id == selectedId }
    val open = articles.firstOrNull { it.id == selectedId } ?: navList.getOrNull(selectedIndex)
    LaunchedEffect(open) {
        onReadingChange(open != null)
    }
    if (open != null) {
        BackHandler {
            selectedId = null
            readingOrder = emptyList()
        }
        val prevIndex = ArticleNav.step(selectedIndex, navList.size, -1)
        val nextIndex = ArticleNav.step(selectedIndex, navList.size, 1)
        ArticleReaderScreen(
            article = open,
            settings = readerSettings,
            isOnline = isOnline,
            onBack = {
                selectedId = null
                readingOrder = emptyList()
            },
            onPrev =
                prevIndex?.let { index ->
                    {
                        val next = navList[index]
                        selectedId = next.id
                        onArticleOpen(next)
                    }
                },
            onNext =
                nextIndex?.let { index ->
                    {
                        val next = navList[index]
                        selectedId = next.id
                        onArticleOpen(next)
                    }
                },
            starred = starredIds.contains(open.id),
            onToggleStar = { onToggleStar(open) },
            onParkUnread = { onParkUnread(open) },
            initialMode = savedMode,
            onModeChange = { mode -> scope.launch { browsePrefs.setReaderMode(mode) } },
            modifier = modifier,
        )
        return
    }
    ReaderSplit(
        nodes = tree,
        selectedId = nodeId,
        onSelect = { id -> scope.launch { browsePrefs.setNodeId("news", id) } },
        unreadOnly = unreadOnly,
        onUnreadOnly = { value -> scope.launch { browsePrefs.setUnreadOnly("news", value) } },
        unreadLabel = stringResource(R.string.filter_unread),
        sort = sort,
        onSort = { next -> scope.launch { browsePrefs.setSort("news", next) } },
        modifier = modifier.fillMaxSize(),
    ) {
        if (visible.isEmpty() && articles.isNotEmpty()) {
            Text(
                text = stringResource(R.string.filter_empty),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("news-filter-empty"),
            )
        } else {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("news-list"),
    ) {
        items(visible, key = { it.id }) { article ->
            val starred = starredIds.contains(article.id)
            val unread = unreadIds.contains(article.id)
            ListItem(
                modifier = Modifier
                    .testTag("news-article-${article.id}")
                    .clickable {
                        onArticleOpen(article)
                        if (readerSettings.target == ArticleTarget.LAUNCHER) {
                            readingOrder = visible
                            selectedId = article.id
                        } else {
                            ArticleOpen.openBrowser(context, article.url)
                        }
                    },
                headlineContent = {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (unread) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                },
                supportingContent = article.summary?.let { summary ->
                    {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                        )
                    }
                },
                leadingContent = if (unread) {
                    {
                        Text(
                            text = stringResource(R.string.news_unread_marker),
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
                            contentDescription = stringResource(
                                if (starred) R.string.news_star_remove_cd else R.string.news_star_add_cd,
                            ),
                            tint = if (starred) {
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
    }
}

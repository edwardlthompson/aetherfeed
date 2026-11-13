package org.aetherfeed.app.ui.podcasts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.aetherfeed.app.R
import org.aetherfeed.app.browse.BrowsePrefs
import org.aetherfeed.app.browse.ContentFilter
import org.aetherfeed.app.browse.ModuleBrowseState
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.ui.browse.ReaderSplit

@Composable
fun PodcastsScreen(
    shows: List<Feed>,
    starredIds: Set<String> = emptySet(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val browsePrefs = remember { BrowsePrefs(context) }
    val browse by browsePrefs.moduleState("podcasts").collectAsState(ModuleBrowseState())
    val unreadOnly = browse.unreadOnly
    val nodeId = browse.nodeId
    val sort = browse.sort
    val starredLabel = stringResource(R.string.filter_starred)
    val allLabel = stringResource(R.string.filter_all)
    val tree =
        remember(shows, starredLabel, allLabel, unreadOnly) {
            ContentFilter.feedTree(
                feeds = shows.map { it.url to it.title },
                hideEmpty = unreadOnly,
                allLabel = allLabel,
                starredLabel = starredLabel,
            )
        }
    val visible =
        remember(shows, unreadOnly, nodeId, sort, starredIds) {
            ContentFilter.sortBy(
                ContentFilter.apply(
                    shows,
                    unreadOnly = false,
                    starredOnly = nodeId == "starred",
                    inCategory = { ContentFilter.matchesFolder(nodeId, it.url, it.id) },
                    isUnread = { true },
                    isStarred = { starredIds.contains(it.id) || starredIds.contains(it.url) },
                ),
                sort,
            ) { it.updatedAt }
        }
    if (shows.isEmpty()) {
        Text(
            text = stringResource(R.string.pane_podcasts_body),
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.fillMaxSize().testTag("podcasts-empty"),
        )
        return
    }
    ReaderSplit(
        nodes = tree,
        selectedId = nodeId,
        onSelect = { id -> scope.launch { browsePrefs.setNodeId("podcasts", id) } },
        unreadOnly = unreadOnly,
        onUnreadOnly = { value -> scope.launch { browsePrefs.setUnreadOnly("podcasts", value) } },
        unreadLabel = stringResource(R.string.filter_unread),
        sort = sort,
        onSort = { next -> scope.launch { browsePrefs.setSort("podcasts", next) } },
        modifier = modifier.fillMaxSize(),
    ) {
        if (visible.isEmpty()) {
            Text(
                text = stringResource(R.string.filter_empty),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("podcasts-filter-empty"),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().testTag("podcasts-list")) {
                items(visible, key = { it.id }) { show ->
                    ListItem(
                        headlineContent = { Text(show.title) },
                        supportingContent = { Text(show.url) },
                        modifier = Modifier.testTag("podcast-show-${show.id}"),
                    )
                }
            }
        }
    }
}

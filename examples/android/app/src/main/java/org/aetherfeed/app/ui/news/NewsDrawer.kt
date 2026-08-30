package org.aetherfeed.app.ui.news

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.ui.theme.SpacingMd

@Composable
internal fun NewsFolderDrawer(
    groups: List<Pair<String, List<Feed>>>,
    selectedFolder: String?,
    selectedFeedId: String?,
    expanded: Set<String>,
    onFolder: (String) -> Unit,
    onFeed: (Feed) -> Unit,
    unreadByFolder: Map<String, Int> = emptyMap(),
    unreadByFeed: Map<String, Int> = emptyMap(),
    library: LibraryTreeState? = null,
    onToggle: (String) -> Unit = onFolder,
) {
    ModalDrawerSheet(Modifier.fillMaxHeight().clipToBounds().testTag("news-sources-drawer")) {
        Text(
            text = stringResource(R.string.news_sources),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(SpacingMd),
        )
        if (groups.isEmpty()) {
            Text(
                text = stringResource(R.string.news_empty),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = SpacingMd),
            )
            return@ModalDrawerSheet
        }
        if (library != null) {
            LibraryTree(
                pick = library.pick,
                newsGroups = library.newsGroups,
                podcastFeeds = library.podcastFeeds,
                boardFeeds = library.boardFeeds,
                expanded = expanded,
                unreadNews = library.unreadNews,
                unreadPodcasts = library.unreadPodcasts,
                unreadBoards = library.unreadBoards,
                unreadByFolder = unreadByFolder,
                unreadByFeed = unreadByFeed,
                onToggle = onToggle,
                onPick = { pick -> library.onPick(pick) },
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        } else {
            NewsSourceTree(
                groups = groups,
                selectedFolder = selectedFolder,
                selectedFeedId = selectedFeedId,
                expanded = expanded,
                onFolder = onFolder,
                onFeed = onFeed,
                unreadByFolder = unreadByFolder,
                unreadByFeed = unreadByFeed,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

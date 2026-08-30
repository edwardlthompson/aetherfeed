package org.aetherfeed.app.ui.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.navigation.LibraryPick
import org.aetherfeed.app.ui.navigation.destination
import org.aetherfeed.app.ui.theme.SpacingMd
import org.aetherfeed.app.ui.theme.SpacingXs

internal data class LibraryTreeState(
    val pick: LibraryPick,
    val newsGroups: List<Pair<String, List<Feed>>>,
    val podcastFeeds: List<Feed>,
    val boardFeeds: List<Feed>,
    val unreadNews: Int,
    val unreadPodcasts: Int,
    val unreadBoards: Int,
    val onPick: (LibraryPick) -> Unit,
)

@Composable
internal fun LibraryTree(
    pick: LibraryPick,
    newsGroups: List<Pair<String, List<Feed>>>,
    podcastFeeds: List<Feed>,
    boardFeeds: List<Feed>,
    expanded: Set<String>,
    unreadNews: Int,
    unreadPodcasts: Int,
    unreadBoards: Int,
    unreadByFolder: Map<String, Int>,
    unreadByFeed: Map<String, Int>,
    onToggle: (String) -> Unit,
    onPick: (LibraryPick) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState()).testTag("news-folders")) {
        Column(Modifier.testTag("news-feeds")) {
            RootRow("unified", stringResource(R.string.nav_unified), pick is LibraryPick.Unified, unreadNews + unreadPodcasts + unreadBoards, "unified-nav-unread", expanded, onToggle) {
                onPick(LibraryPick.Unified)
            }
            RootRow(AppDestination.News.name, stringResource(R.string.nav_news), pick.destination() == AppDestination.News && pick !is LibraryPick.Unified, unreadNews, "news-nav-unread", expanded, onToggle) {
                onPick(LibraryPick.All(AppDestination.News))
            }
            if (AppDestination.News.name in expanded) {
                NewsSourceTree(
                    newsGroups, folderOf(pick), sourceId(pick), expanded,
                    { onPick(LibraryPick.Folder(AppDestination.News, it)) },
                    { feed -> onPick(LibraryPick.Source(AppDestination.News, folderName(newsGroups, feed), feed.id)) },
                    unreadByFolder, unreadByFeed, onToggle = onToggle, wrap = false,
                )
            }
            RootRow(AppDestination.Podcasts.name, stringResource(R.string.nav_podcasts), pick.destination() == AppDestination.Podcasts, unreadPodcasts, "podcast-nav-unread", expanded, onToggle) {
                onPick(LibraryPick.All(AppDestination.Podcasts))
            }
            if (AppDestination.Podcasts.name in expanded) {
                FeedChildren(podcastFeeds, pick, AppDestination.Podcasts, unreadByFeed, onPick)
            }
            RootRow(AppDestination.Booru.name, stringResource(R.string.nav_booru), pick.destination() == AppDestination.Booru, unreadBoards, "boards-nav-unread", expanded, onToggle) {
                onPick(LibraryPick.All(AppDestination.Booru))
            }
            if (AppDestination.Booru.name in expanded) {
                FeedChildren(boardFeeds, pick, AppDestination.Booru, unreadByFeed, onPick)
            }
        }
    }
}

@Composable
private fun RootRow(
    key: String,
    label: String,
    selected: Boolean,
    unread: Int,
    unreadTag: String,
    expanded: Set<String>,
    onToggle: (String) -> Unit,
    onSelect: () -> Unit,
) {
    val open = key in expanded
    val accent = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingXs),
        modifier = Modifier.fillMaxWidth().clipToBounds().padding(vertical = SpacingXs),
    ) {
        Icon(
            imageVector = if (open) Icons.Outlined.ExpandMore else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = stringResource(if (open) R.string.news_folder_collapse else R.string.news_folder_expand),
            tint = accent,
            modifier = Modifier.size(18.dp).clickable { onToggle(key) },
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).clickable(onClick = onSelect),
        )
        NewsUnreadBadge(unread, unreadTag)
    }
}

@Composable
private fun FeedChildren(
    feeds: List<Feed>,
    pick: LibraryPick,
    mode: AppDestination,
    unreadByFeed: Map<String, Int>,
    onPick: (LibraryPick) -> Unit,
) {
    val selected = (pick as? LibraryPick.Source)?.sourceId
    feeds.forEach { feed ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .clickable { onPick(LibraryPick.Source(mode, feed.folder ?: mode.name, feed.id)) }
                .padding(start = SpacingMd, top = SpacingXs, bottom = SpacingXs),
        ) {
            Text(
                text = feed.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (feed.id == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            NewsUnreadBadge(unreadByFeed[feed.id] ?: 0, "news-feed-unread")
        }
    }
}

private fun folderOf(pick: LibraryPick): String? = when (pick) {
    is LibraryPick.Folder -> pick.folder
    is LibraryPick.Source -> pick.folder
    else -> null
}

private fun sourceId(pick: LibraryPick): String? = (pick as? LibraryPick.Source)?.sourceId

private fun folderName(groups: List<Pair<String, List<Feed>>>, feed: Feed): String =
    groups.firstOrNull { pair -> pair.second.any { it.id == feed.id } }?.first ?: feed.folder.orEmpty()

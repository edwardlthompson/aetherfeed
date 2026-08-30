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
import org.aetherfeed.app.ui.theme.SpacingMd
import org.aetherfeed.app.ui.theme.SpacingXs

@Composable
internal fun NewsUnreadBadge(count: Int, tag: String) {
    if (count <= 0) return
    Text(
        text = count.toString(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.testTag(tag).padding(start = SpacingXs),
    )
}

@Composable
internal fun NewsSourceTree(
    groups: List<Pair<String, List<Feed>>>,
    selectedFolder: String?,
    selectedFeedId: String?,
    expanded: Set<String>,
    onFolder: (String) -> Unit,
    onFeed: (Feed) -> Unit,
    unreadByFolder: Map<String, Int> = emptyMap(),
    unreadByFeed: Map<String, Int> = emptyMap(),
    onToggle: ((String) -> Unit)? = null,
    wrap: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val body = @Composable {
        groups.forEach { (folder, feeds) ->
            val key = "${org.aetherfeed.app.ui.navigation.AppDestination.News.name}:$folder"
            val open = folder in expanded || key in expanded
            val accent = if (selectedFolder == folder) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingXs),
                modifier = Modifier.fillMaxWidth().clipToBounds().padding(vertical = SpacingXs).testTag("news-folder"),
            ) {
                Icon(
                    imageVector = if (open) Icons.Outlined.ExpandMore else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = stringResource(
                        if (open) R.string.news_folder_collapse else R.string.news_folder_expand,
                    ),
                    tint = accent,
                    modifier = Modifier.size(18.dp).clickable { (onToggle ?: onFolder)(folder) },
                )
                Text(
                    text = folder,
                    style = MaterialTheme.typography.titleSmall,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).clickable { onFolder(folder) },
                )
                NewsUnreadBadge(unreadByFolder[folder] ?: 0, "news-folder-unread")
            }
            if (open) {
                feeds.forEach { feed ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clipToBounds()
                            .clickable { onFeed(feed) }
                            .padding(start = SpacingMd, top = SpacingXs, bottom = SpacingXs),
                    ) {
                        Text(
                            text = feed.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (feed.id == selectedFeedId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        NewsUnreadBadge(unreadByFeed[feed.id] ?: 0, "news-feed-unread")
                    }
                }
            }
        }
    }
    if (wrap) {
        Column(modifier = modifier.verticalScroll(rememberScrollState()).testTag("news-folders")) {
            Column(Modifier.testTag("news-feeds")) { body() }
        }
    } else {
        Column(modifier) { body() }
    }
}

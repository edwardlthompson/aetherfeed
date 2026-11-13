package org.aetherfeed.app.ui.booru

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.aetherfeed.app.browse.TreeNode
import org.aetherfeed.app.domain.BooruPost
import org.aetherfeed.app.ui.browse.ReaderSplit
import org.aetherfeed.app.ui.theme.SpacingMd
import org.aetherfeed.app.ui.theme.SpacingSm

@Composable
fun BooruScreen(
    posts: List<BooruPost>,
    starredIds: Set<String>,
    onToggleStar: (BooruPost) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<BooruPost?>(null) }
    val post = selected
    if (post != null) {
        BooruPostDetail(
            post = post,
            starred = starredIds.contains(post.id),
            onBack = { selected = null },
            onToggleStar = { onToggleStar(post) },
            modifier = modifier,
        )
    } else {
        BooruPostGrid(
            posts = posts,
            starredIds = starredIds,
            onPostOpen = { selected = it },
            onToggleStar = onToggleStar,
            modifier = modifier,
        )
    }
}

@Composable
private fun BooruPostGrid(
    posts: List<BooruPost>,
    starredIds: Set<String>,
    onPostOpen: (BooruPost) -> Unit,
    onToggleStar: (BooruPost) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val browsePrefs = remember { BrowsePrefs(context) }
    val browse by browsePrefs.moduleState("booru").collectAsState(ModuleBrowseState())
    val unreadOnly = browse.unreadOnly
    val nodeId = browse.nodeId
    val sort = browse.sort
    val starredLabel = stringResource(R.string.filter_starred)
    val tags =
        remember(posts, starredLabel) {
            listOf(TreeNode("", ""), TreeNode("starred", starredLabel)) +
                posts.flatMap { it.tags }.groupingBy { it }.eachCount().entries
                    .sortedByDescending { it.value }
                    .take(12)
                    .map { TreeNode(it.key, it.key) }
        }
    val visible =
        remember(posts, unreadOnly, nodeId, sort, starredIds) {
            ContentFilter.sortBy(
                ContentFilter.apply(
                    posts,
                    unreadOnly = false,
                    starredOnly = nodeId == "starred",
                    inCategory = { nodeId.isEmpty() || nodeId == "starred" || it.tags.contains(nodeId) },
                    isUnread = { true },
                    isStarred = { starredIds.contains(it.id) },
                ),
                sort,
            ) { 0L }
        }
    ReaderSplit(
        nodes = tags,
        selectedId = nodeId,
        onSelect = { id -> scope.launch { browsePrefs.setNodeId("booru", id) } },
        unreadOnly = unreadOnly,
        onUnreadOnly = { value -> scope.launch { browsePrefs.setUnreadOnly("booru", value) } },
        unreadLabel = stringResource(R.string.filter_unread),
        sort = sort,
        onSort = { next -> scope.launch { browsePrefs.setSort("booru", next) } },
        modifier = modifier.fillMaxSize(),
    ) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = SpacingMd * 10),
        modifier = Modifier
            .fillMaxSize()
            .testTag("booru-grid"),
        contentPadding = PaddingValues(SpacingMd),
        horizontalArrangement = Arrangement.spacedBy(SpacingSm),
        verticalArrangement = Arrangement.spacedBy(SpacingSm),
    ) {
        items(visible, key = { it.id }) { post ->
            val starred = starredIds.contains(post.id)
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booru-post-${post.id}")
                    .clickable { onPostOpen(post) },
                headlineContent = {
                    Text(
                        text = stringResource(R.string.booru_post_label, post.remoteId),
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
                supportingContent = {
                    Text(
                        text = post.tags.take(3).joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        modifier = Modifier.testTag("booru-tags-${post.id}"),
                    )
                },
                trailingContent = {
                    IconButton(
                        onClick = { onToggleStar(post) },
                        modifier = Modifier.testTag("booru-star-${post.id}"),
                    ) {
                        Icon(
                            imageVector = if (starred) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = stringResource(
                                if (starred) R.string.booru_star_remove_cd else R.string.booru_star_add_cd,
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

@Composable
private fun BooruPostDetail(
    post: BooruPost,
    starred: Boolean,
    onBack: () -> Unit,
    onToggleStar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("booru-detail")
            .padding(SpacingMd),
    ) {
        ListItem(
            leadingContent = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("booru-back"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.booru_back_cd),
                    )
                }
            },
            headlineContent = {
                Text(
                    text = stringResource(R.string.booru_post_label, post.remoteId),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            trailingContent = {
                IconButton(
                    onClick = onToggleStar,
                    modifier = Modifier.testTag("booru-detail-star"),
                ) {
                    Icon(
                        imageVector = if (starred) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = stringResource(
                            if (starred) R.string.booru_star_remove_cd else R.string.booru_star_add_cd,
                        ),
                    )
                }
            },
        )
        Text(
            text = stringResource(R.string.booru_detail_tags_heading),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = SpacingSm),
        )
        Text(
            text = post.tags.joinToString(" "),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = SpacingSm)
                .testTag("booru-detail-tags"),
        )
        Text(
            text = post.previewUrl ?: post.fileUrl,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = SpacingMd),
        )
    }
}

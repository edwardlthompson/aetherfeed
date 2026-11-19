package org.aetherfeed.shared.ui.booru

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.aetherfeed.shared.domain.BooruPost
import org.aetherfeed.shared.ui.theme.SpacingMd
import org.aetherfeed.shared.ui.theme.SpacingSm

data class BooruLabels(
    val postLabel: (remoteId: String) -> String,
    val tagsHeading: String,
    val backCd: String,
    val starAddCd: String,
    val starRemoveCd: String,
)

@Composable
fun BooruScreen(
    posts: List<BooruPost>,
    starredIds: Set<String>,
    labels: BooruLabels,
    onToggleStar: (BooruPost) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<BooruPost?>(null) }
    val post = selected
    if (post != null) {
        BooruPostDetail(
            post = post,
            starred = post.id in starredIds,
            labels = labels,
            onBack = { selected = null },
            onToggleStar = { onToggleStar(post) },
            modifier = modifier,
        )
    } else {
        BooruPostGrid(
            posts = posts,
            starredIds = starredIds,
            labels = labels,
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
    labels: BooruLabels,
    onPostOpen: (BooruPost) -> Unit,
    onToggleStar: (BooruPost) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = SpacingMd * 10),
        modifier = modifier.fillMaxSize().testTag("booru-grid"),
        contentPadding = PaddingValues(SpacingMd),
        horizontalArrangement = Arrangement.spacedBy(SpacingSm),
        verticalArrangement = Arrangement.spacedBy(SpacingSm),
    ) {
        items(posts, key = { it.id }) { post ->
            val starred = post.id in starredIds
            ListItem(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("booru-post-${post.id}")
                        .clickable { onPostOpen(post) },
                headlineContent = {
                    Text(labels.postLabel(post.remoteId), style = MaterialTheme.typography.titleSmall)
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
                            contentDescription = if (starred) labels.starRemoveCd else labels.starAddCd,
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

@Composable
private fun BooruPostDetail(
    post: BooruPost,
    starred: Boolean,
    labels: BooruLabels,
    onBack: () -> Unit,
    onToggleStar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().testTag("booru-detail").padding(SpacingMd)) {
        ListItem(
            leadingContent = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("booru-back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = labels.backCd)
                }
            },
            headlineContent = {
                Text(labels.postLabel(post.remoteId), style = MaterialTheme.typography.titleLarge)
            },
            trailingContent = {
                IconButton(onClick = onToggleStar, modifier = Modifier.testTag("booru-detail-star")) {
                    Icon(
                        imageVector = if (starred) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (starred) labels.starRemoveCd else labels.starAddCd,
                    )
                }
            },
        )
        Text(
            text = labels.tagsHeading,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = SpacingSm),
        )
        Text(
            text = post.tags.joinToString(" "),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = SpacingSm).testTag("booru-detail-tags"),
        )
        Text(
            text = post.previewUrl ?: post.fileUrl,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = SpacingMd),
        )
    }
}

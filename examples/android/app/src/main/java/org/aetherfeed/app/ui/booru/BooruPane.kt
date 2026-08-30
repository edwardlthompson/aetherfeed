package org.aetherfeed.app.ui.booru

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.aetherfeed.app.R
import org.aetherfeed.app.booru.BoardsSnapshot
import org.aetherfeed.app.booru.BooruSession
import org.aetherfeed.app.booru.LocalBooruClient
import org.aetherfeed.app.domain.BooruPost
import org.aetherfeed.app.ui.ModeActionBus
import org.aetherfeed.app.ui.theme.SpacingMd
import org.aetherfeed.app.ui.theme.SpacingSm

@Composable
fun BooruPane(
    modifier: Modifier = Modifier,
    session: BooruSession? = null,
    actions: ModeActionBus? = null,
) {
    val boards = session ?: remember { BooruSession(LocalBooruClient()) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var sourceUrl by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var posts by remember { mutableStateOf(listOf<BooruPost>()) }
    var favorites by remember { mutableStateOf(listOf<BooruPost>()) }
    var searchGen by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<BooruPost?>(null) }
    var infoText by remember { mutableStateOf("") }

    fun runSearch() {
        searchGen += 1
        val gen = searchGen
        scope.launch {
            val snap = runCatching { boards.search(query) }.getOrElse {
                BoardsSnapshot(emptyList(), boards.favorites(), failed = true)
            }
            if (gen == searchGen) {
                posts = BooruPaneLogic.filterPosts(snap.posts)
                favorites = snap.favorites
            }
        }
    }
    DisposableEffect(selected?.id, actions) {
        if (actions != null) {
            actions.favorite = selected?.let { post ->
                {
                    boards.toggleFavorite(post)
                    favorites = boards.favorites()
                    actions.publish()
                }
            }
            actions.info = selected?.let { post ->
                { infoText = post.tags.joinToString(" ").ifBlank { post.remoteId } }
            }
            val href = selected?.fileUrl?.trim()?.takeIf { it.isNotEmpty() }
            actions.share = href?.let { url ->
                { org.aetherfeed.app.ui.navigation.shareText(context, url) }
            }
            actions.publish()
        }
        onDispose { actions?.clearBoards() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingMd),
        verticalArrangement = Arrangement.Top,
    ) {
        OutlinedTextField(
            value = sourceUrl,
            onValueChange = { sourceUrl = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SpacingMd)
                .testTag("boards-source-url"),
            singleLine = true,
            placeholder = { Text("https://") },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("boards-search"),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.nav_booru)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { runSearch() }),
            )
            IconButton(
                onClick = { runSearch() },
                modifier = Modifier.testTag("boards-search-go"),
            ) {
                Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.nav_booru_cd))
            }
        }
        if (BooruPaneLogic.showDesignedEmpty(posts.size)) {
            Text(
                text = stringResource(R.string.pane_booru_body),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(top = SpacingMd)
                    .testTag("boards-empty"),
            )
            Text(
                text = stringResource(R.string.local_only_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = SpacingMd),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(top = SpacingMd)
                    .testTag("boards-grid"),
                horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                verticalArrangement = Arrangement.spacedBy(SpacingSm),
            ) {
                items(posts, key = { it.id }) { post ->
                    BoardCell(
                        post = post,
                        favorited = boards.isFavorite(post.id),
                        onSelect = { selected = post },
                        onFavorite = {
                            boards.toggleFavorite(post)
                            favorites = boards.favorites()
                        },
                    )
                }
            }
        }
        if (infoText.isNotBlank()) {
            Text(
                text = infoText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = SpacingSm).testTag("boards-info"),
            )
        }
        favorites.forEach { post ->
            Text(
                text = post.tags.joinToString(" ").ifBlank { post.remoteId },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(top = SpacingSm)
                    .testTag("boards-favorite-${post.id}"),
            )
        }
    }
}

@Composable
private fun BoardCell(
    post: BooruPost,
    favorited: Boolean,
    onSelect: () -> Unit,
    onFavorite: () -> Unit,
) {
    Surface(
        onClick = onSelect,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.testTag("boards-post-${post.id}"),
    ) {
        Column(modifier = Modifier.padding(SpacingSm)) {
            Text(
                text = post.tags.joinToString(" ").ifBlank { post.remoteId },
                style = MaterialTheme.typography.bodySmall,
            )
            IconButton(onClick = onFavorite, modifier = Modifier.testTag("boards-star-${post.id}")) {
                Icon(
                    imageVector = if (favorited) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                    contentDescription = post.tags.firstOrNull() ?: post.remoteId,
                )
            }
        }
    }
}

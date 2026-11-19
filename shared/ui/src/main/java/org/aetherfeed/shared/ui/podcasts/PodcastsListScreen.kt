package org.aetherfeed.shared.ui.podcasts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.aetherfeed.shared.domain.Feed

/** Podcast show list (DESIGN_BRIEF §4). Empty body matches Android STUB. */
@Composable
fun PodcastsListScreen(
    shows: List<Feed>,
    emptyLabel: String,
    modifier: Modifier = Modifier,
) {
    if (shows.isEmpty()) {
        Text(
            text = emptyLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.fillMaxSize().testTag("podcasts-empty"),
        )
        return
    }
    LazyColumn(modifier = modifier.fillMaxSize().testTag("podcasts-list")) {
        items(shows, key = { it.id }) { show ->
            ListItem(
                headlineContent = { Text(show.title) },
                supportingContent = { Text(show.url) },
                modifier = Modifier.testTag("podcast-show-${show.id}"),
            )
        }
    }
}

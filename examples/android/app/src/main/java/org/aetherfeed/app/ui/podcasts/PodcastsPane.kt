package org.aetherfeed.app.ui.podcasts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import org.aetherfeed.app.data.RoomLibraryRepository
import org.aetherfeed.app.data.SqlCipherVault
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.downloads.createAppDownloadQueue
import org.aetherfeed.app.downloads.isUnmeteredNetwork
import org.aetherfeed.app.news.HttpFeedFetcher
import org.aetherfeed.app.podcasts.Media3PodcastPlayer
import org.aetherfeed.app.podcasts.enclosureFromRss
import org.aetherfeed.app.ui.ModeActionBus
import org.aetherfeed.app.ui.theme.SpacingLg
import org.aetherfeed.app.ui.theme.SpacingMd

internal const val PLAY_LABEL = "Play"
internal const val PAUSE_LABEL = "Pause"
internal const val PLAYBACK_FAILED = "Playback failed"
internal const val DOWNLOADS_IDLE = "No downloads"

@Composable
fun PodcastsPane(
    modifier: Modifier = Modifier,
    actions: ModeActionBus? = null,
    pick: org.aetherfeed.app.ui.navigation.LibraryPick = org.aetherfeed.app.ui.navigation.LibraryPick.All(
        org.aetherfeed.app.ui.navigation.AppDestination.Podcasts,
    ),
    unread: org.aetherfeed.app.ui.UnreadCounts = org.aetherfeed.app.ui.UnreadCounts(),
    onLibraryPick: (org.aetherfeed.app.ui.navigation.LibraryPick) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val enclosures = remember { mutableMapOf<String, String>() }
    val player = remember {
        Media3PodcastPlayer(context) { id -> enclosures[id] }
    }
    val downloads = remember {
        createAppDownloadQueue(context.filesDir) { isUnmeteredNetwork(context) }
    }
    val library = remember {
        runCatching { RoomLibraryRepository(SqlCipherVault().open(context).libraryDao()) }.getOrNull()
    }
    var shows by remember { mutableStateOf(listOf<Feed>()) }
    var playingId by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val emptyHint = stringResource(R.string.readerimport_podcasts_hint)
    fun togglePlay() {
        scope.launch {
            runCatching {
                val id = playingId ?: shows.firstOrNull()?.id ?: return@launch
                if (playingId == id) {
                    player.pause()
                    playingId = null
                } else {
                    player.play(id)
                    playingId = id
                }
                errorText = null
            }.onFailure { errorText = it.message ?: PLAYBACK_FAILED }
        }
    }
    fun skipBy(deltaMs: Long) {
        val id = playingId ?: return
        scope.launch {
            val pos = player.position(id)?.positionMs.let { it ?: 0L }
            player.seekTo((pos + deltaMs).coerceAtLeast(0L))
        }
    }
    DisposableEffect(shows, playingId, actions) {
        if (actions != null) {
            actions.play = { togglePlay() }
            actions.skipBack = { skipBy(-15_000L) }
            actions.skipFwd = { skipBy(15_000L) }
            actions.playing = playingId != null
            val href = shows.firstOrNull { it.id == playingId }?.url ?: shows.firstOrNull()?.url
            actions.share = href?.let { url -> { org.aetherfeed.app.ui.navigation.shareText(context, url) } }
            actions.publish()
        }
        onDispose { actions?.clearPodcasts() }
    }
    LaunchedEffect(library) {
        if (library != null) {
            shows = runCatching { library.feeds().filter { it.kind == ModuleKind.Podcast } }.getOrDefault(emptyList())
            val fetcher = HttpFeedFetcher()
            shows.forEach { show ->
                val body = runCatching { fetcher.fetch(show.url, 8_000L) }.getOrNull() ?: return@forEach
                val url = enclosureFromRss(body)
                if (url.isNotBlank()) {
                    enclosures[show.id] = url
                    runCatching {
                        downloads.enqueue(
                            org.aetherfeed.app.domain.Episode(show.id, show.id, show.title, url),
                        )
                    }
                }
            }
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingMd)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
    ) {
        org.aetherfeed.app.ui.navigation.RememberedLibraryTree(pick, unread, onLibraryPick)
        Text(
            text = emptyHint,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = SpacingLg),
        )
        Button(
            onClick = { togglePlay() },
            modifier = Modifier
                .padding(top = SpacingMd)
                .testTag("podcasts-play"),
        ) {
            Text(if (playingId != null) PAUSE_LABEL else PLAY_LABEL)
        }
        Text(
            text = if (downloads.snapshot().isEmpty()) DOWNLOADS_IDLE else downloads.snapshot().size.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = SpacingMd)
                .testTag("podcasts-downloads"),
        )
        errorText?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = SpacingMd),
            )
        }
        if (shows.isEmpty()) {
            Text(
                text = stringResource(R.string.pane_podcasts_body),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = SpacingMd),
            )
        } else {
            shows.forEach { show ->
                Text(text = show.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = SpacingMd))
            }
        }
    }
}

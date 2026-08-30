package org.aetherfeed.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.aetherfeed.app.data.RoomLibraryRepository
import org.aetherfeed.app.data.SqlCipherVault
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.news.groupFeedsByFolder
import org.aetherfeed.app.ui.UnreadCounts
import org.aetherfeed.app.ui.news.LibraryTree

@Composable
fun RememberedLibraryTree(
    pick: LibraryPick,
    unread: UnreadCounts,
    onPick: (LibraryPick) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var extra by remember { mutableStateOf(listOf<Feed>()) }
    var expanded by remember { mutableStateOf(pick.expandKeys()) }
    LaunchedEffect(Unit) {
        extra = runCatching { RoomLibraryRepository(SqlCipherVault().open(context).libraryDao()).feeds() }.getOrDefault(emptyList())
        expanded = expanded + pick.expandKeys()
    }
    LibraryTree(
        pick = pick,
        newsGroups = groupFeedsByFolder(extra.filter { it.kind == ModuleKind.News }),
        podcastFeeds = extra.filter { it.kind == ModuleKind.Podcast },
        boardFeeds = extra.filter { it.kind == ModuleKind.Booru },
        expanded = expanded,
        unreadNews = unread.news,
        unreadPodcasts = unread.podcasts,
        unreadBoards = unread.boards,
        unreadByFolder = emptyMap(),
        unreadByFeed = emptyMap(),
        onToggle = { key -> expanded = if (key in expanded) expanded - key else expanded + key },
        onPick = onPick,
        modifier = modifier,
    )
}

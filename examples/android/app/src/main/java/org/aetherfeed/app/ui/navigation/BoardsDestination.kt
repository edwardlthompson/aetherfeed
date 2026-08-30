package org.aetherfeed.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.aetherfeed.app.data.RoomLibraryRepository
import org.aetherfeed.app.data.SqlCipherVault
import org.aetherfeed.app.ui.ModeActionBus
import org.aetherfeed.app.ui.UnreadCounts
import org.aetherfeed.app.ui.booru.BooruPane
import androidx.compose.foundation.layout.Column

@Composable
fun BoardsDestination(
    modifier: Modifier = Modifier,
    actions: ModeActionBus? = null,
    pick: LibraryPick = LibraryPick.All(AppDestination.Booru),
    unread: UnreadCounts = UnreadCounts(),
    onLibraryPick: (LibraryPick) -> Unit = {},
) {
    val context = LocalContext.current
    val library = remember {
        runCatching { RoomLibraryRepository(SqlCipherVault().open(context).libraryDao()) }.getOrNull()
    }
    var sourceCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(library) {
        sourceCount = runCatching { library?.feeds()?.let(::countBoardSources) ?: 0 }.getOrDefault(0)
    }
    Column(modifier) {
        RememberedLibraryTree(pick, unread, onLibraryPick)
        if (shouldShowBoardsEmpty(sourceCount)) {
            BoardsEmptyChrome(sourceCount = sourceCount)
        } else {
            BooruPane(actions = actions)
        }
    }
}

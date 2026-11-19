package org.aetherfeed.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.aetherfeed.shared.ui.components.AetherFeedScaffold as SharedScaffold

@Composable
fun AetherFeedScaffold(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    SharedScaffold(
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        content = content,
    )
}

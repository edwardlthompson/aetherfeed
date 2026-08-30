package org.aetherfeed.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource

@Composable
fun BoardsEmptyChrome(
    modifier: Modifier = Modifier,
    sourceCount: Int = 0,
) {
    if (!shouldShowBoardsEmpty(sourceCount)) return
    ModulePane(
        body = stringResource(boardsEmptyBodyRes()),
        modifier = modifier.testTag(BOARDS_EMPTY_TEST_TAG),
    )
}

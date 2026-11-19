package org.aetherfeed.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.shared.domain.ModuleKind as SharedKind
import org.aetherfeed.shared.ui.feed.AddFeedDialog as SharedAddFeedDialog
import org.aetherfeed.shared.ui.feed.AddFeedFab as SharedAddFeedFab
import org.aetherfeed.shared.ui.feed.AddFeedLabels

@Composable
fun AddFeedFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SharedAddFeedFab(
        contentDescription = stringResource(R.string.add_feed_fab_cd),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun AddFeedDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, title: String, kind: ModuleKind) -> Unit,
) {
    SharedAddFeedDialog(
        labels =
            AddFeedLabels(
                title = stringResource(R.string.add_feed_title),
                url = stringResource(R.string.add_feed_url),
                titleOptional = stringResource(R.string.add_feed_title_optional),
                confirm = stringResource(R.string.add_feed_confirm),
                cancel = stringResource(R.string.add_feed_cancel),
                kindLabels =
                    mapOf(
                        SharedKind.News to stringResource(R.string.nav_news),
                        SharedKind.Podcast to stringResource(R.string.nav_podcasts),
                        SharedKind.Booru to stringResource(R.string.nav_booru),
                    ),
            ),
        onDismiss = onDismiss,
        onConfirm = { url, title, kind -> onConfirm(url, title, kind.toApp()) },
    )
}

private fun SharedKind.toApp(): ModuleKind =
    when (this) {
        SharedKind.News -> ModuleKind.News
        SharedKind.Podcast -> ModuleKind.Podcast
        SharedKind.Booru -> ModuleKind.Booru
    }

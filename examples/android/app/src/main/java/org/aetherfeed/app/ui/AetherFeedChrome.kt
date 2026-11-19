package org.aetherfeed.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.shared.ui.AetherFeedBottomBar as SharedBottomBar
import org.aetherfeed.shared.ui.AetherFeedTopBar as SharedTopBar
import org.aetherfeed.shared.ui.navigation.AppDestination as SharedDestination

@Composable
fun AetherFeedBottomBar(
    destination: AppDestination,
    onSelect: (AppDestination) -> Unit,
) {
    SharedBottomBar(
        destination = destination.toShared(),
        labels =
            mapOf(
                SharedDestination.News to stringResource(R.string.nav_news),
                SharedDestination.Podcasts to stringResource(R.string.nav_podcasts),
                SharedDestination.Booru to stringResource(R.string.nav_booru),
            ),
        contentDescriptions =
            mapOf(
                SharedDestination.News to stringResource(R.string.nav_news_cd),
                SharedDestination.Podcasts to stringResource(R.string.nav_podcasts_cd),
                SharedDestination.Booru to stringResource(R.string.nav_booru_cd),
            ),
        onSelect = { onSelect(it.toApp()) },
    )
}

@Composable
fun AetherFeedTopBar(
    onSettingsOpen: () -> Unit,
) {
    SharedTopBar(
        title = stringResource(R.string.app_title),
        settingsContentDescription = stringResource(R.string.settings_open),
        onOpenSettings = onSettingsOpen,
    )
}

private fun AppDestination.toShared(): SharedDestination =
    when (this) {
        AppDestination.News -> SharedDestination.News
        AppDestination.Podcasts -> SharedDestination.Podcasts
        AppDestination.Booru -> SharedDestination.Booru
    }

private fun SharedDestination.toApp(): AppDestination =
    when (this) {
        SharedDestination.News -> AppDestination.News
        SharedDestination.Podcasts -> AppDestination.Podcasts
        SharedDestination.Booru -> AppDestination.Booru
    }

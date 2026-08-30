package org.aetherfeed.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.ui.components.ThemeToggle
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.navigation.modeTabDestinations
import org.aetherfeed.app.ui.theme.ThemeMode

data class UnreadCounts(val news: Int = 0, val podcasts: Int = 0, val boards: Int = 0)

@Composable
fun AetherFeedBottomBar(
    destination: AppDestination,
    unread: UnreadCounts,
    onSelect: (AppDestination) -> Unit,
) {
    NavigationBar {
        modeTabDestinations().forEach { item ->
            val count = when (item) {
                AppDestination.News -> unread.news
                AppDestination.Podcasts -> unread.podcasts
                AppDestination.Booru -> unread.boards
            }
            val tag = when (item) {
                AppDestination.News -> "news-nav-unread"
                AppDestination.Podcasts -> "podcast-nav-unread"
                AppDestination.Booru -> "boards-nav-unread"
            }
            NavigationBarItem(
                selected = destination == item,
                onClick = { onSelect(item) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (count > 0) Badge(Modifier.testTag(tag)) { Text(count.toString()) }
                        },
                    ) {
                        Icon(item.icon, contentDescription = stringResource(item.contentDescriptionRes))
                    }
                },
                label = { Text(stringResource(item.labelRes)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AetherFeedTopBar(
    themeMode: ThemeMode,
    onThemeToggle: () -> Unit,
    onSettingsOpen: () -> Unit,
    onAboutOpen: () -> Unit,
    onRefresh: (() -> Unit)? = null,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_title)) },
        actions = {
            if (onRefresh != null) {
                IconButton(onClick = onRefresh, modifier = Modifier.testTag("news-refresh")) {
                    Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.news_refresh))
                }
            }
            IconButton(onClick = onSettingsOpen) {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_open))
            }
            IconButton(onClick = onAboutOpen) {
                Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.about_open))
            }
            ThemeToggle(themeMode = themeMode, onToggle = onThemeToggle)
        },
    )
}

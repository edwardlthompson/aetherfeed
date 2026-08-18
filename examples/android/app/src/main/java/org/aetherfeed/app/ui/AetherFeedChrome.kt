package org.aetherfeed.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.ui.components.ThemeToggle
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.theme.ThemeMode

@Composable
fun AetherFeedBottomBar(
    destination: AppDestination,
    onSelect: (AppDestination) -> Unit,
) {
    NavigationBar {
        AppDestination.entries.forEach { item ->
            NavigationBarItem(
                selected = destination == item,
                onClick = { onSelect(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.contentDescriptionRes),
                    )
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
) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_title)) },
        actions = {
            IconButton(onClick = onSettingsOpen) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings_open),
                )
            }
            IconButton(onClick = onAboutOpen) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(R.string.about_open),
                )
            }
            ThemeToggle(themeMode = themeMode, onToggle = onThemeToggle)
        },
    )
}

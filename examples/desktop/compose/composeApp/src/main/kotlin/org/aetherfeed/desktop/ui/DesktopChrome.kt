package org.aetherfeed.desktop.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import org.aetherfeed.desktop.ui.navigation.AppDestination

@Composable
fun AetherFeedBottomBar(
    destination: AppDestination,
    onSelect: (AppDestination) -> Unit,
) {
    val labels =
        mapOf(
            AppDestination.News to "News",
            AppDestination.Podcasts to "Podcasts",
            AppDestination.Booru to "Booru",
        )
    NavigationBar {
        AppDestination.entries.forEach { item ->
            NavigationBarItem(
                selected = destination == item,
                onClick = { onSelect(item) },
                icon = { Icon(item.icon, contentDescription = labels.getValue(item)) },
                label = { Text(labels.getValue(item)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AetherFeedTopBar(onOpenSettings: () -> Unit) {
    TopAppBar(
        title = { Text("AetherFeed") },
        actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        },
    )
}
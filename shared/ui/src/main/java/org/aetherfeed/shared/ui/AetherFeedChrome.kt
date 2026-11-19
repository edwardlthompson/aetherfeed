package org.aetherfeed.shared.ui

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
import org.aetherfeed.shared.ui.navigation.AppDestination

@Composable
fun AetherFeedBottomBar(
    destination: AppDestination,
    labels: Map<AppDestination, String>,
    contentDescriptions: Map<AppDestination, String>,
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
                        contentDescription = contentDescriptions.getValue(item),
                    )
                },
                label = { Text(labels.getValue(item)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AetherFeedTopBar(
    title: String,
    settingsContentDescription: String,
    onOpenSettings: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = settingsContentDescription,
                )
            }
        },
    )
}

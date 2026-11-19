package org.aetherfeed.desktop.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val icon: ImageVector,
) {
    News(Icons.Outlined.Home),
    Podcasts(Icons.Outlined.PlayArrow),
    Booru(Icons.Outlined.Favorite),
}
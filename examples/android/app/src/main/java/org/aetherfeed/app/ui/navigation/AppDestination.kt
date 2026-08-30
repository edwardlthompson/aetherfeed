package org.aetherfeed.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.ui.graphics.vector.ImageVector
import org.aetherfeed.app.R

enum class AppDestination(
    val labelRes: Int,
    val contentDescriptionRes: Int,
    val icon: ImageVector,
) {
    News(R.string.nav_news, R.string.nav_news_cd, Icons.Outlined.Article),
    Podcasts(R.string.nav_podcasts, R.string.nav_podcasts_cd, Icons.Outlined.Podcasts),
    Booru(R.string.nav_booru, R.string.nav_booru_cd, Icons.Outlined.Image),
}

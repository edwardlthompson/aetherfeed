package org.aetherfeed.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import org.aetherfeed.app.ui.insets.ApplySystemBarStyle
import org.aetherfeed.shared.ui.theme.AetherFeedTheme as SharedAetherFeedTheme

@Composable
fun AetherFeedTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme =
        when (themeMode) {
            ThemeMode.System -> systemDark
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }
    ApplySystemBarStyle(darkTheme)
    SharedAetherFeedTheme(themeMode = themeMode, content = content)
}

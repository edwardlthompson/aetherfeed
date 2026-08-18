package org.aetherfeed.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.aetherfeed.app.ui.insets.ApplySystemBarStyle

@Composable
fun AetherFeedTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colorScheme = if (darkTheme) DarkAetherFeedColors else LightAetherFeedColors

    ApplySystemBarStyle(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AetherFeedTypography,
        content = content,
    )
}

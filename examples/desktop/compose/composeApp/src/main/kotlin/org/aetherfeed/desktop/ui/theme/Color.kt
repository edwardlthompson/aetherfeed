// Palette aligned with design-tokens / shared/ui Color.kt (desktop-local until CMP cutover).
package org.aetherfeed.desktop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val AfLightPrimary = Color(0xFF3D5A6C)
private val AfDarkPrimary = Color(0xFF8FB4C7)
private val AfLightBackground = Color(0xFFF7F6F2)
private val AfDarkBackground = Color(0xFF12161A)
private val AfLightOnBackground = Color(0xFF1A1F24)
private val AfDarkOnBackground = Color(0xFFE6E4DC)
private val AfLightSurface = Color(0xFFF1F0EB)
private val AfDarkSurface = Color(0xFF1A1F24)

val LightAetherFeedColors = lightColorScheme(
    primary = AfLightPrimary,
    onPrimary = Color(0xFFFFFFFF),
    background = AfLightBackground,
    onBackground = AfLightOnBackground,
    surface = AfLightSurface,
    onSurface = AfLightOnBackground,
)

val DarkAetherFeedColors = darkColorScheme(
    primary = AfDarkPrimary,
    onPrimary = Color(0xFF12161A),
    background = AfDarkBackground,
    onBackground = AfDarkOnBackground,
    surface = AfDarkSurface,
    onSurface = AfDarkOnBackground,
)
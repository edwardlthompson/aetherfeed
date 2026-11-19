package org.aetherfeed.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.news.ArticleTarget
import org.aetherfeed.app.news.ImagePolicy
import org.aetherfeed.app.news.ReaderSettings
import org.aetherfeed.app.ui.theme.ThemeMode
import org.aetherfeed.shared.domain.ArticleTarget as SharedTarget
import org.aetherfeed.shared.domain.ImagePolicy as SharedImages
import org.aetherfeed.shared.domain.ReaderSettings as SharedReaderSettings
import org.aetherfeed.shared.ui.settings.SettingsLabels
import org.aetherfeed.shared.ui.settings.SettingsScreen as SharedSettingsScreen

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    updateCheckEnabled: Boolean,
    hermesPoolStatus: String = "",
    lanPoolStatus: String = "",
    lanHubUrl: String = "",
    lanPassphrase: String = "",
    onThemeModeSelect: (ThemeMode) -> Unit,
    onUpdateCheckChange: (Boolean) -> Unit,
    onLanSave: (hubUrl: String, passphrase: String) -> Unit = { _, _ -> },
    onLanSync: () -> Unit = {},
    readerSettings: ReaderSettings = ReaderSettings(),
    onReaderTarget: (ArticleTarget) -> Unit = {},
    onReaderScale: (Float) -> Unit = {},
    onReaderImages: (ImagePolicy) -> Unit = {},
    onAboutOpen: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    SharedSettingsScreen(
        labels =
            SettingsLabels(
                title = stringResource(R.string.settings_title),
                themeLabel = stringResource(R.string.settings_theme_label),
                themeModes =
                    mapOf(
                        ThemeMode.System to stringResource(R.string.settings_theme_mode_system),
                        ThemeMode.Light to stringResource(R.string.settings_theme_mode_light),
                        ThemeMode.Dark to stringResource(R.string.settings_theme_mode_dark),
                    ),
                updateCheck = stringResource(R.string.settings_update_check_label),
                hermesTitle = stringResource(R.string.settings_hermes_pool_title),
                hermesIdle = stringResource(R.string.settings_hermes_pool_idle),
                lanTitle = stringResource(R.string.settings_lan_pool_title),
                lanHint = stringResource(R.string.settings_lan_pool_hint),
                lanHub = stringResource(R.string.settings_lan_pool_hub),
                lanPassphrase = stringResource(R.string.settings_lan_pool_passphrase),
                lanIdle = stringResource(R.string.settings_lan_pool_idle),
                lanSave = stringResource(R.string.settings_lan_pool_save),
                lanSync = stringResource(R.string.settings_lan_pool_sync),
                aboutOpen = stringResource(R.string.about_open),
                close = stringResource(R.string.settings_close),
                openerTitle = stringResource(R.string.feed_opener_title),
                openerBody = stringResource(R.string.feed_opener_body),
                openerTargets =
                    mapOf(
                        SharedTarget.LAUNCHER to stringResource(R.string.feed_opener_launcher),
                        SharedTarget.BROWSER to stringResource(R.string.feed_opener_browser),
                        SharedTarget.CUSTOM_TAB to stringResource(R.string.feed_opener_custom_tab),
                    ),
                scaleTitle = stringResource(R.string.feed_reader_scale),
                scalePercent = { pct -> context.getString(R.string.feed_reader_scale_percent, pct) },
                imagesTitle = stringResource(R.string.feed_images_title),
                imagesBody = stringResource(R.string.feed_images_body),
                imagePolicies =
                    mapOf(
                        SharedImages.ALWAYS to stringResource(R.string.feed_images_always),
                        SharedImages.WIFI to stringResource(R.string.feed_images_wifi),
                        SharedImages.NEVER to stringResource(R.string.feed_images_never),
                    ),
            ),
        themeMode = themeMode,
        updateCheckEnabled = updateCheckEnabled,
        hermesPoolStatus = hermesPoolStatus,
        lanPoolStatus = lanPoolStatus,
        lanHubUrl = lanHubUrl,
        lanPassphrase = lanPassphrase,
        readerSettings = readerSettings.toShared(),
        onThemeModeSelect = onThemeModeSelect,
        onUpdateCheckChange = onUpdateCheckChange,
        onLanSave = onLanSave,
        onLanSync = onLanSync,
        onReaderTarget = { onReaderTarget(it.toApp()) },
        onReaderScale = onReaderScale,
        onReaderImages = { onReaderImages(it.toApp()) },
        onAboutOpen = onAboutOpen,
        onBack = onBack,
        modifier = modifier,
    )
}

private fun ReaderSettings.toShared() =
    SharedReaderSettings(
        target = SharedTarget.valueOf(target.name),
        bodyScale = bodyScale,
        imagePolicy = SharedImages.valueOf(imagePolicy.name),
    )

private fun SharedTarget.toApp(): ArticleTarget = ArticleTarget.valueOf(name)

private fun SharedImages.toApp(): ImagePolicy = ImagePolicy.valueOf(name)

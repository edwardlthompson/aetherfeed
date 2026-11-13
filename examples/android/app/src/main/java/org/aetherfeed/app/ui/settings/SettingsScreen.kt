package org.aetherfeed.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.news.ArticleTarget
import org.aetherfeed.app.news.ImagePolicy
import org.aetherfeed.app.news.ReaderSettings
import org.aetherfeed.app.ui.insets.bottomInsetPadding
import org.aetherfeed.app.ui.theme.SpacingMd
import org.aetherfeed.app.ui.theme.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
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
    var hubDraft by remember(lanHubUrl) { mutableStateOf(lanHubUrl) }
    var passDraft by remember(lanPassphrase) { mutableStateOf(lanPassphrase) }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("settings-heading"),
        )
        Text(text = stringResource(R.string.settings_theme_label))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = themeMode == mode,
                    onClick = { onThemeModeSelect(mode) },
                    label = {
                        Text(
                            when (mode) {
                                ThemeMode.System -> stringResource(R.string.settings_theme_mode_system)
                                ThemeMode.Light -> stringResource(R.string.settings_theme_mode_light)
                                ThemeMode.Dark -> stringResource(R.string.settings_theme_mode_dark)
                            },
                        )
                    },
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            Text(
                text = stringResource(R.string.settings_update_check_label),
                modifier = Modifier.weight(1f),
            )
            Switch(checked = updateCheckEnabled, onCheckedChange = onUpdateCheckChange)
        }
        SettingsReaderSection(
            settings = readerSettings,
            onTarget = onReaderTarget,
            onScale = onReaderScale,
            onImages = onReaderImages,
        )
        Text(
            text = stringResource(R.string.settings_hermes_pool_title),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text =
                hermesPoolStatus.ifBlank {
                    stringResource(R.string.settings_hermes_pool_idle)
                },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.testTag("settings-hermes-pool"),
        )
        Text(
            text = stringResource(R.string.settings_lan_pool_title),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = stringResource(R.string.settings_lan_pool_hint),
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = hubDraft,
            onValueChange = { hubDraft = it },
            label = { Text(stringResource(R.string.settings_lan_pool_hub)) },
            singleLine = true,
            modifier = Modifier.testTag("settings-lan-hub"),
        )
        OutlinedTextField(
            value = passDraft,
            onValueChange = { passDraft = it },
            label = { Text(stringResource(R.string.settings_lan_pool_passphrase)) },
            singleLine = true,
            modifier = Modifier.testTag("settings-lan-pass"),
        )
        Text(
            text =
                lanPoolStatus.ifBlank {
                    stringResource(R.string.settings_lan_pool_idle)
                },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.testTag("settings-lan-status"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            OutlinedButton(onClick = { onLanSave(hubDraft, passDraft) }) {
                Text(stringResource(R.string.settings_lan_pool_save))
            }
            Button(onClick = onLanSync, modifier = Modifier.testTag("settings-lan-sync")) {
                Text(stringResource(R.string.settings_lan_pool_sync))
            }
        }
        OutlinedButton(
            onClick = onAboutOpen,
            modifier = Modifier.testTag("settings-about"),
        ) {
            Text(stringResource(R.string.about_open))
        }
        Button(
            onClick = onBack,
            modifier = Modifier.bottomInsetPadding(),
        ) {
            Text(stringResource(R.string.settings_close))
        }
    }
}

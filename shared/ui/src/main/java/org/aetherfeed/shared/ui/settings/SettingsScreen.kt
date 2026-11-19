package org.aetherfeed.shared.ui.settings

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
import org.aetherfeed.shared.domain.ArticleTarget
import org.aetherfeed.shared.domain.ImagePolicy
import org.aetherfeed.shared.domain.ReaderScale
import org.aetherfeed.shared.domain.ReaderSettings
import org.aetherfeed.shared.ui.theme.SpacingMd
import org.aetherfeed.shared.ui.theme.ThemeMode
import kotlin.math.abs

/** Settings field order matches DESIGN_BRIEF §6 / Android SettingsScreen. */
data class SettingsLabels(
    val title: String,
    val themeLabel: String,
    val themeModes: Map<ThemeMode, String>,
    val updateCheck: String,
    val hermesTitle: String,
    val hermesIdle: String,
    val lanTitle: String,
    val lanHint: String,
    val lanHub: String,
    val lanPassphrase: String,
    val lanIdle: String,
    val lanSave: String,
    val lanSync: String,
    val aboutOpen: String,
    val close: String,
    val openerTitle: String,
    val openerBody: String,
    val openerTargets: Map<ArticleTarget, String>,
    val scaleTitle: String,
    val scalePercent: (Int) -> String,
    val imagesTitle: String,
    val imagesBody: String,
    val imagePolicies: Map<ImagePolicy, String>,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    labels: SettingsLabels,
    themeMode: ThemeMode,
    updateCheckEnabled: Boolean,
    showHermesBlock: Boolean = true,
    hermesPoolStatus: String = "",
    lanPoolStatus: String = "",
    lanHubUrl: String = "",
    lanPassphrase: String = "",
    readerSettings: ReaderSettings = ReaderSettings(),
    onThemeModeSelect: (ThemeMode) -> Unit,
    onUpdateCheckChange: (Boolean) -> Unit,
    onLanSave: (hubUrl: String, passphrase: String) -> Unit = { _, _ -> },
    onLanSync: () -> Unit = {},
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
        modifier = modifier.verticalScroll(rememberScrollState()).padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(labels.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("settings-heading"))
        Text(labels.themeLabel)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = themeMode == mode,
                    onClick = { onThemeModeSelect(mode) },
                    label = { Text(labels.themeModes.getValue(mode)) },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            Text(labels.updateCheck, modifier = Modifier.weight(1f))
            Switch(checked = updateCheckEnabled, onCheckedChange = onUpdateCheckChange)
        }
        Text(labels.openerTitle, style = MaterialTheme.typography.titleSmall, modifier = Modifier.testTag("settings-article-opener"))
        Text(labels.openerBody, style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ArticleTarget.entries.forEach { target ->
                FilterChip(
                    selected = readerSettings.target == target,
                    onClick = { onReaderTarget(target) },
                    label = { Text(labels.openerTargets.getValue(target)) },
                    modifier = Modifier.testTag("settings-opener-${target.name.lowercase()}"),
                )
            }
        }
        Text(labels.scaleTitle, style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            listOf(ReaderScale.MIN, 1.0f, 1.15f, 1.3f, 1.45f, ReaderScale.MAX).forEach { scale ->
                FilterChip(
                    selected = abs(readerSettings.bodyScale - scale) < 0.01f,
                    onClick = { onReaderScale(scale) },
                    label = { Text(labels.scalePercent((scale * 100).toInt())) },
                )
            }
        }
        Text(labels.imagesTitle, style = MaterialTheme.typography.titleSmall)
        Text(labels.imagesBody, style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ImagePolicy.entries.forEach { policy ->
                FilterChip(
                    selected = readerSettings.imagePolicy == policy,
                    onClick = { onReaderImages(policy) },
                    label = { Text(labels.imagePolicies.getValue(policy)) },
                    modifier = Modifier.testTag("settings-images-${policy.name.lowercase()}"),
                )
            }
        }
        if (showHermesBlock) {
            Text(labels.hermesTitle, style = MaterialTheme.typography.titleSmall)
            Text(
                hermesPoolStatus.ifBlank { labels.hermesIdle },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("settings-hermes-pool"),
            )
        }
        Text(labels.lanTitle, style = MaterialTheme.typography.titleSmall)
        Text(labels.lanHint, style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = hubDraft,
            onValueChange = { hubDraft = it },
            label = { Text(labels.lanHub) },
            singleLine = true,
            modifier = Modifier.testTag("settings-lan-hub"),
        )
        OutlinedTextField(
            value = passDraft,
            onValueChange = { passDraft = it },
            label = { Text(labels.lanPassphrase) },
            singleLine = true,
            modifier = Modifier.testTag("settings-lan-pass"),
        )
        Text(
            lanPoolStatus.ifBlank { labels.lanIdle },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.testTag("settings-lan-status"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            OutlinedButton(onClick = { onLanSave(hubDraft, passDraft) }) { Text(labels.lanSave) }
            Button(onClick = onLanSync, modifier = Modifier.testTag("settings-lan-sync")) { Text(labels.lanSync) }
        }
        OutlinedButton(onClick = onAboutOpen, modifier = Modifier.testTag("settings-about")) { Text(labels.aboutOpen) }
        Button(onClick = onBack) { Text(labels.close) }
    }
}

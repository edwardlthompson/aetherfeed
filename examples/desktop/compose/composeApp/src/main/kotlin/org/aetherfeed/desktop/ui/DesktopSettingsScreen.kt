package org.aetherfeed.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import kotlin.math.abs
import org.aetherfeed.desktop.sync.LanHubHost
import org.aetherfeed.desktop.ui.theme.SpacingMd
import org.aetherfeed.desktop.ui.theme.ThemeMode
import org.aetherfeed.shared.domain.ArticleTarget
import org.aetherfeed.shared.domain.ImagePolicy
import org.aetherfeed.shared.domain.ReaderScale
import org.aetherfeed.shared.domain.ReaderSettings

/** Field order matches DESIGN_BRIEF §6 (Hermes block omitted on desktop). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesktopSettingsScreen(
    themeMode: ThemeMode,
    updateCheckEnabled: Boolean,
    readerSettings: ReaderSettings,
    lanHubUrl: String,
    lanPassphrase: String,
    lanPoolStatus: String,
    lanHub: LanHubHost,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onUpdateCheckChange: (Boolean) -> Unit,
    onReaderTarget: (ArticleTarget) -> Unit,
    onReaderScale: (Float) -> Unit,
    onReaderImages: (ImagePolicy) -> Unit,
    onLanSave: (hubUrl: String, passphrase: String) -> Unit,
    onLanSync: () -> Unit,
    onAboutOpen: () -> Unit,
    onBack: () -> Unit,
) {
    var hubDraft by remember(lanHubUrl) { mutableStateOf(lanHubUrl) }
    var passDraft by remember(lanPassphrase) { mutableStateOf(lanPassphrase) }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Text("Theme")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ThemeMode.entries.forEach { mode ->
                val label =
                    when (mode) {
                        ThemeMode.System -> "System"
                        ThemeMode.Light -> "Light"
                        ThemeMode.Dark -> "Dark"
                    }
                FilterChip(
                    selected = themeMode == mode,
                    onClick = { onThemeModeSelect(mode) },
                    label = { Text(label) },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            Text("Check for updates", modifier = Modifier.weight(1f))
            Switch(checked = updateCheckEnabled, onCheckedChange = onUpdateCheckChange)
        }
        Text("Preferred opener", style = MaterialTheme.typography.titleSmall)
        Text("Open articles in the in-app reader or an external browser.", style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ArticleTarget.entries.forEach { target ->
                FilterChip(
                    selected = readerSettings.target == target,
                    onClick = { onReaderTarget(target) },
                    label = { Text(target.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }
        Text("Text size", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            listOf(ReaderScale.MIN, 1.0f, 1.15f, 1.3f, 1.45f, ReaderScale.MAX).forEach { scale ->
                FilterChip(
                    selected = abs(readerSettings.bodyScale - scale) < 0.01f,
                    onClick = { onReaderScale(scale) },
                    label = { Text("${(scale * 100).toInt()}%") },
                )
            }
        }
        Text("Article images", style = MaterialTheme.typography.titleSmall)
        Text("Control when downloaded images appear in the reader.", style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ImagePolicy.entries.forEach { policy ->
                FilterChip(
                    selected = readerSettings.imagePolicy == policy,
                    onClick = { onReaderImages(policy) },
                    label = { Text(policy.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }
        Text("Home LAN pool", style = MaterialTheme.typography.titleSmall)
        Text(
            "Host encrypted subscription pool on this device (port ${lanHub.status().port}).",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = hubDraft,
            onValueChange = { hubDraft = it },
            label = { Text("Hub URL") },
            singleLine = true,
        )
        OutlinedTextField(
            value = passDraft,
            onValueChange = { passDraft = it },
            label = { Text("Shared passphrase") },
            singleLine = true,
        )
        Text(
            lanPoolStatus.ifBlank { "LAN pool idle." },
            style = MaterialTheme.typography.bodySmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            OutlinedButton(onClick = { onLanSave(hubDraft, passDraft) }) { Text("Save LAN settings") }
            Button(onClick = onLanSync) { Text("Sync now") }
        }
        OutlinedButton(onClick = onAboutOpen) { Text("About") }
        Button(onClick = onBack) { Text("Close settings") }
    }
}
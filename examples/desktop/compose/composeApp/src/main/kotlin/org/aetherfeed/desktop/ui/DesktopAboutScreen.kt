package org.aetherfeed.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.aetherfeed.desktop.ui.theme.SpacingMd
import org.aetherfeed.shared.domain.DonationsConfig

@Composable
fun DesktopAboutScreen(
    version: String,
    installedFormat: String,
    updateStatus: String,
    donations: DonationsConfig,
    onBack: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text("About", style = MaterialTheme.typography.headlineSmall)
        Text("Version $version")
        Text("Format: $installedFormat")
        Text(updateStatus)
        Text(
            "Encrypted vault export will ship in a later row — local-only for now.",
            style = MaterialTheme.typography.bodyMedium,
        )
        if (donations.enabled && donations.links.isNotEmpty()) {
            Text("Donations", style = MaterialTheme.typography.titleMedium)
            Text(donations.message)
            donations.links.forEach { link ->
                Text(link.label, color = MaterialTheme.colorScheme.primary)
            }
        }
        Button(onClick = onBack) { Text("Close") }
    }
}
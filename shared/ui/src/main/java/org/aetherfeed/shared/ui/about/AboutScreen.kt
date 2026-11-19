package org.aetherfeed.shared.ui.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import org.aetherfeed.shared.domain.DonationsConfig
import org.aetherfeed.shared.ui.theme.SpacingMd

data class AboutLabels(
    val title: String,
    val version: (String) -> String,
    val format: (String) -> String,
    val exportHint: String,
    val donationsHeading: String,
    val updateApply: String,
    val close: String,
)

@Composable
fun AboutScreen(
    labels: AboutLabels,
    version: String,
    installedFormat: String,
    updateStatus: String,
    donations: DonationsConfig,
    canApplyUpdate: Boolean,
    onApplyUpdate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(SpacingMd)
                .testTag("about-panel"),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(labels.title, style = MaterialTheme.typography.headlineSmall)
        Text(labels.version(version))
        Text(labels.format(installedFormat))
        Text(updateStatus)
        if (canApplyUpdate) {
            Button(onClick = onApplyUpdate) { Text(labels.updateApply) }
        }
        Text(labels.exportHint, style = MaterialTheme.typography.bodyMedium)
        if (donations.enabled && donations.links.isNotEmpty()) {
            Text(labels.donationsHeading, style = MaterialTheme.typography.titleMedium)
            Text(donations.message)
            donations.links.forEach { link ->
                Text(
                    text = link.label,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { uriHandler.openUri(link.url) },
                )
            }
        }
        Button(onClick = onBack) { Text(labels.close) }
    }
}

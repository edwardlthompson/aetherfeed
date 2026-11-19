package org.aetherfeed.app.ui.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.about.DonationsConfig
import org.aetherfeed.shared.domain.DonationLink as SharedLink
import org.aetherfeed.shared.domain.DonationsConfig as SharedDonations
import org.aetherfeed.shared.ui.about.AboutLabels
import org.aetherfeed.shared.ui.about.AboutScreen as SharedAboutScreen

@Composable
fun AboutScreen(
    version: String,
    installedFormat: String,
    updateStatus: String,
    donations: DonationsConfig,
    canApplyUpdate: Boolean,
    onApplyUpdate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    SharedAboutScreen(
        labels =
            AboutLabels(
                title = stringResource(R.string.about_title),
                version = { v -> context.getString(R.string.about_version, v) },
                format = { f -> context.getString(R.string.about_format, f) },
                exportHint = stringResource(R.string.about_export_hint),
                donationsHeading = stringResource(R.string.about_donations_heading),
                updateApply = stringResource(R.string.about_update_apply),
                close = stringResource(R.string.about_close),
            ),
        version = version,
        installedFormat = installedFormat,
        updateStatus = updateStatus,
        donations =
            SharedDonations(
                enabled = donations.enabled,
                message = donations.message,
                links = donations.links.map { SharedLink(it.label, it.url) },
            ),
        canApplyUpdate = canApplyUpdate,
        onApplyUpdate = onApplyUpdate,
        onBack = onBack,
        modifier = modifier,
    )
}

package org.aetherfeed.app.ui.about

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
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.about.DonateLinks
import org.aetherfeed.app.about.DonationsConfig
import org.aetherfeed.app.ui.insets.LocalNavigationMode
import org.aetherfeed.app.ui.insets.bottomInsetPadding
import org.aetherfeed.app.ui.insets.navigationBarInsetBottomDp
import org.aetherfeed.app.ui.insets.navigationModeLabelRes
import org.aetherfeed.app.ui.theme.SpacingMd

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
    val uriHandler = LocalUriHandler.current
    val navMode = LocalNavigationMode.current
    val insetDp = navigationBarInsetBottomDp()
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(text = stringResource(R.string.about_version, version))
        Text(text = stringResource(R.string.about_format, installedFormat))
        Text(text = updateStatus)
        Text(
            text = stringResource(
                R.string.about_debug_navigation_mode,
                stringResource(navigationModeLabelRes(navMode)),
                insetDp,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (canApplyUpdate) {
            Button(onClick = onApplyUpdate) {
                Text(stringResource(R.string.about_update_apply))
            }
        }
        Text(text = donations.message.ifBlank { stringResource(R.string.about_donations_message) })
        Text(
            text = stringResource(R.string.about_donate_venmo),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { uriHandler.openUri(DonateLinks.VENMO_URL) },
        )
        donations.links.filter { it.url != DonateLinks.VENMO_URL }.forEach { link ->
            Text(
                text = link.label,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { uriHandler.openUri(link.url) },
            )
        }
        Button(
            onClick = onBack,
            modifier = Modifier.bottomInsetPadding(),
        ) {
            Text(stringResource(R.string.about_close))
        }
    }
}

package org.aetherfeed.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.ui.theme.SpacingLg
import org.aetherfeed.app.ui.theme.SpacingMd

@Composable
fun ModulePane(
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingMd),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = SpacingLg),
        )
        Text(
            text = stringResource(R.string.local_only_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = SpacingMd),
        )
    }
}

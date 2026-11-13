package org.aetherfeed.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.ui.theme.SpacingMd

@Composable
fun AddFeedFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.testTag("add-feed-fab"),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.add_feed_fab_cd),
        )
    }
}

@Composable
fun AddFeedDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, title: String, kind: ModuleKind) -> Unit,
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(ModuleKind.News) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_feed_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.add_feed_url)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add-feed-url"),
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.add_feed_title_optional)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingMd),
                ) {
                    ModuleKind.entries.forEach { option ->
                        FilterChip(
                            selected = kind == option,
                            onClick = { kind = option },
                            label = {
                                Text(
                                    when (option) {
                                        ModuleKind.News -> stringResource(R.string.nav_news)
                                        ModuleKind.Podcast -> stringResource(R.string.nav_podcasts)
                                        ModuleKind.Booru -> stringResource(R.string.nav_booru)
                                    },
                                )
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(url.trim(), title.trim(), kind) },
                enabled = url.trim().startsWith("http"),
                modifier = Modifier.testTag("add-feed-confirm"),
            ) {
                Text(stringResource(R.string.add_feed_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.add_feed_cancel))
            }
        },
    )
}

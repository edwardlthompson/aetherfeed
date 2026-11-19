package org.aetherfeed.shared.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import org.aetherfeed.shared.domain.ModuleKind
import org.aetherfeed.shared.ui.theme.SpacingMd

@Composable
fun AddFeedFab(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.testTag("add-feed-fab"),
    ) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = contentDescription)
    }
}

data class AddFeedLabels(
    val title: String,
    val url: String,
    val titleOptional: String,
    val confirm: String,
    val cancel: String,
    val kindLabels: Map<ModuleKind, String>,
)

@Composable
fun AddFeedDialog(
    labels: AddFeedLabels,
    onDismiss: () -> Unit,
    onConfirm: (url: String, title: String, kind: ModuleKind) -> Unit,
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(ModuleKind.News) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(labels.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(labels.url) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add-feed-url"),
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(labels.titleOptional) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                    ModuleKind.entries.forEach { option ->
                        FilterChip(
                            selected = kind == option,
                            onClick = { kind = option },
                            label = { Text(labels.kindLabels.getValue(option)) },
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
                Text(labels.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(labels.cancel) }
        },
    )
}

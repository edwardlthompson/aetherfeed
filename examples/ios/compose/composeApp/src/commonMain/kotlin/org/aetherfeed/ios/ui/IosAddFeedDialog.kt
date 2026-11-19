package org.aetherfeed.ios.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.aetherfeed.shared.domain.ModuleKind

/** Same Add subscription dialog on every OS (decision 13, DESIGN_BRIEF §8). */
@Composable
fun IosAddFeedDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, title: String, kind: ModuleKind) -> Unit,
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(ModuleKind.News) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add subscription") },
        text = {
            Column {
                OutlinedTextField(url, { url = it }, label = { Text("Feed URL") })
                OutlinedTextField(title, { title = it }, label = { Text("Title (optional)") })
                ModuleKind.entries.forEach { k ->
                    FilterChip(
                        selected = kind == k,
                        onClick = { kind = k },
                        label = { Text(k.name) },
                    )
                }
            }
        },
        confirmButton = { TextButton({ onConfirm(url.trim(), title.trim(), kind) }) { Text("Add") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
    )
}
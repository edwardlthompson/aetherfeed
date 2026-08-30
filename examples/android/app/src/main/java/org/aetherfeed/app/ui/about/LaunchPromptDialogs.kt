package org.aetherfeed.app.ui.about

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.about.AppLaunchPrompts

@Composable
fun LaunchPromptDialogs(
    prompt: AppLaunchPrompts.Prompt?,
    onDonate: () -> Unit,
    onDonateDismiss: () -> Unit,
    onInstall: (AppLaunchPrompts.Prompt.Update) -> Unit,
    onLater: (AppLaunchPrompts.Prompt.Update) -> Unit,
) {
    when (prompt) {
        AppLaunchPrompts.Prompt.Donate -> AlertDialog(
            onDismissRequest = onDonateDismiss,
            title = { Text(stringResource(R.string.about_donate_nudge_title)) },
            text = { Text(stringResource(R.string.about_donate_nudge_message)) },
            confirmButton = {
                TextButton(onClick = onDonate) {
                    Text(stringResource(R.string.about_donate_venmo))
                }
            },
            dismissButton = {
                TextButton(onClick = onDonateDismiss) {
                    Text(stringResource(R.string.about_not_now))
                }
            },
        )
        is AppLaunchPrompts.Prompt.Update -> AlertDialog(
            onDismissRequest = { onLater(prompt) },
            title = { Text(stringResource(R.string.about_update_available, prompt.version)) },
            text = { Text(stringResource(R.string.about_update_prompt, prompt.version)) },
            confirmButton = {
                TextButton(onClick = { onInstall(prompt) }) {
                    Text(stringResource(R.string.about_install))
                }
            },
            dismissButton = {
                TextButton(onClick = { onLater(prompt) }) {
                    Text(stringResource(R.string.about_later))
                }
            },
        )
        null -> Unit
    }
}

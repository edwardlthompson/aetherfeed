package org.aetherfeed.app.applock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.launch
import org.aetherfeed.app.domain.AppLockSecretKind
import org.aetherfeed.app.domain.AppLockState
import org.aetherfeed.app.domain.classifyLockSecret
import org.aetherfeed.app.ui.theme.SpacingMd
import org.aetherfeed.app.ui.theme.SpacingSm

internal const val LOCK_WIPE_WARN = "Forgot PIN or passphrase wipes the local vault. Recovery is impossible."
internal const val LOCK_WEAK = "Choose a longer PIN or passphrase."
internal const val LOCK_WRONG = "Wrong PIN or passphrase."

@Composable
fun LockScreen(
    session: AppLockSession,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var tick by remember { mutableStateOf(0) }
    val unset = remember(tick) { session.state() == AppLockState.Unset }
    val stored = remember(tick) { session.storedKind() }
    var usePin by remember(tick) { mutableStateOf(stored != AppLockSecretKind.Passphrase) }
    var secret by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val focus = remember { FocusRequester() }
    val submit: () -> Unit = {
        scope.launch {
            if (unset) {
                val kind = classifyLockSecret(secret)
                if (kind == null || (usePin && kind != AppLockSecretKind.Pin)) {
                    error = LOCK_WEAK
                    return@launch
                }
                session.setSecret(secret, kind)
                onUnlocked()
            } else if (session.unlock(secret)) {
                onUnlocked()
            } else {
                error = LOCK_WRONG
            }
        }
    }
    LaunchedEffect(usePin) { runCatching { focus.requestFocus() } }
    Column(modifier = modifier.fillMaxSize().padding(SpacingMd).testTag("unlock-pane")) {
        Text(if (unset) "Set a PIN or passphrase" else "Unlock AetherFeed", style = MaterialTheme.typography.headlineMedium)
        Text(LOCK_WIPE_WARN, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = SpacingMd))
        if (unset || stored == null) {
            Row(Modifier.padding(top = SpacingSm).testTag("lock-kind")) {
                FilterChip(selected = usePin, onClick = { usePin = true; secret = "" }, label = { Text("PIN") })
                FilterChip(
                    selected = !usePin,
                    onClick = { usePin = false; secret = "" },
                    label = { Text("Passphrase") },
                    modifier = Modifier.padding(start = SpacingSm),
                )
            }
        }
        OutlinedTextField(
            value = secret,
            onValueChange = { next -> secret = if (usePin) next.filter(Char::isDigit) else next },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (usePin) KeyboardType.NumberPassword else KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.padding(top = SpacingMd).focusRequester(focus).testTag("lock-secret"),
        )
        Button(onClick = submit, modifier = Modifier.padding(top = SpacingMd).testTag("lock-submit")) {
            Text(if (unset) "Save lock" else "Unlock")
        }
        if (!unset) {
            Button(
                onClick = {
                    scope.launch {
                        session.wipe()
                        tick += 1
                        secret = ""
                    }
                },
                modifier = Modifier.padding(top = SpacingMd).testTag("lock-wipe"),
            ) { Text("Wipe vault") }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = SpacingMd)) }
    }
}

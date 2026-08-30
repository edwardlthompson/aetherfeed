package org.aetherfeed.app.applock

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.aetherfeed.app.data.VaultKeyStore

@Composable
fun rememberAppLockSession(context: Context): AppLockSession =
    remember {
        AppLockSession(
            filesDir = context.filesDir,
            seedVaultKey = {
                runCatching { VaultKeyStore(context).adoptAndClearLegacy() }.getOrNull()
            },
        ).also { AppLockHolder.session = it }
    }

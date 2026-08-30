package org.aetherfeed.app.ui.about

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.aetherfeed.app.about.AppLaunchPrompts
import org.aetherfeed.app.about.DonateLinks
import org.aetherfeed.app.about.GithubRelease
import org.aetherfeed.app.about.ProductUpdate
import org.aetherfeed.app.about.UpdateLaunchPrefs

@Composable
fun AppLaunchGate(
    context: Context,
    appVersion: String,
    checksEnabled: Boolean,
) {
    var prompt by remember { mutableStateOf<AppLaunchPrompts.Prompt?>(null) }
    val prefs = remember { UpdateLaunchPrefs(context) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(appVersion, checksEnabled) {
        val now = System.currentTimeMillis()
        val lastSeen = prefs.lastSeenVersion()
        if (ProductUpdate.shouldNudgeDonate(lastSeen, appVersion)) {
            prompt = AppLaunchPrompts.Prompt.Donate
            return@LaunchedEffect
        }
        prefs.markVersionSeen(appVersion)
        val lastCheck = prefs.lastCheckAt()
        if (!checksEnabled || !ProductUpdate.shouldCheckDaily(lastCheck, now)) return@LaunchedEffect
        val release = withContext(Dispatchers.IO) {
            GithubRelease.fetchLatest("AetherFeed/$appVersion")
        }
        val decided = AppLaunchPrompts.decide(
            lastSeenVersion = appVersion,
            currentVersion = appVersion,
            lastCheckAt = lastCheck,
            dismissedVersion = prefs.dismissedVersion(),
            now = now,
            checksEnabled = true,
            release = release,
        )
        if (decided.checked) prefs.markChecked(now)
        prompt = decided.prompt
    }

    LaunchPromptDialogs(
        prompt = prompt,
        onDonate = {
            prefs.markVersionSeen(appVersion)
            prompt = null
            uriHandler.openUri(DonateLinks.VENMO_URL)
        },
        onDonateDismiss = {
            prefs.markVersionSeen(appVersion)
            prompt = null
        },
        onInstall = { update ->
            prefs.markChecked(System.currentTimeMillis())
            prompt = null
            uriHandler.openUri(update.url)
        },
        onLater = { update ->
            prefs.markChecked(System.currentTimeMillis(), update.version)
            prompt = null
        },
    )
}

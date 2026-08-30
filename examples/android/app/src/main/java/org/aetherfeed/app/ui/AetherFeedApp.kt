package org.aetherfeed.app.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.aetherfeed.app.BuildConfig
import org.aetherfeed.app.R
import org.aetherfeed.app.about.AppUpdatePreferences
import org.aetherfeed.app.about.DonationsLoader
import org.aetherfeed.app.about.ReleaseAsset
import org.aetherfeed.app.about.UpdateApplyCoordinator
import org.aetherfeed.app.ui.about.AppLaunchGate
import org.aetherfeed.app.network.NetworkStatusMonitor
import org.aetherfeed.app.settings.SettingsLogic
import androidx.compose.material3.SnackbarHostState
import org.aetherfeed.app.ui.insets.NavigationModeProvider
import org.aetherfeed.app.ui.theme.ThemeMode
import org.aetherfeed.app.ui.theme.ThemePreferences
import org.aetherfeed.app.ui.theme.next
import kotlinx.coroutines.CoroutineScope
import org.aetherfeed.app.ui.theme.AetherFeedTheme
import kotlinx.coroutines.launch

@Composable
fun AetherFeedApp(
    context: Context,
    scope: CoroutineScope,
    themePreferences: ThemePreferences,
    appUpdatePreferences: AppUpdatePreferences,
    networkStatusMonitor: NetworkStatusMonitor,
) {
    val themeMode by themePreferences.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.System)
    val isOnline by networkStatusMonitor.isOnline.collectAsStateWithLifecycle(initialValue = true)
    val installedFormat by appUpdatePreferences.installedFormat.collectAsStateWithLifecycle(initialValue = "apk")
    val checkInterval by appUpdatePreferences.checkInterval.collectAsStateWithLifecycle(initialValue = "daily")
    val pendingRestart by appUpdatePreferences.pendingRestart.collectAsStateWithLifecycle(initialValue = false)
    val appLock = org.aetherfeed.app.applock.rememberAppLockSession(context)
    var unlocked by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf(context.getString(R.string.about_update_current)) }
    var applyAsset by remember { mutableStateOf<ReleaseAsset?>(null) }
    val donations = remember { DonationsLoader.load(context) }
    val appVersion = BuildConfig.VERSION_NAME
    val activity = context as? ComponentActivity

    LaunchedEffect(pendingRestart) {
        if (pendingRestart) {
            updateStatus = context.getString(R.string.about_update_restarting)
        }
    }

    val canApplyUpdate = applyAsset != null
    val snackbarHostState = remember { SnackbarHostState() }

    AetherFeedTheme(themeMode = themeMode) {
        NavigationModeProvider {
            org.aetherfeed.app.applock.WatchAppLockTimeout(
                session = appLock,
                unlocked = unlocked,
                onLock = { unlocked = false },
            )
            if (!unlocked) {
                org.aetherfeed.app.applock.LockScreen(
                    session = appLock,
                    onUnlocked = { unlocked = true },
                )
                return@NavigationModeProvider
            }
            if (showSettings) BackHandler { showSettings = false }
            else if (showAbout) BackHandler { showAbout = false }
            AppLaunchGate(
                context = context,
                appVersion = appVersion,
                checksEnabled = SettingsLogic.isUpdateCheckEnabled(checkInterval),
            )
            AetherFeedScreen(
                snackbarHostState = snackbarHostState,
                themeMode = themeMode,
                isOnline = isOnline,
                showAbout = showAbout,
                showSettings = showSettings,
                updateCheckEnabled = SettingsLogic.isUpdateCheckEnabled(checkInterval),
                appVersion = appVersion,
                installedFormat = installedFormat ?: "apk",
                updateStatus = updateStatus,
                donations = donations,
                canApplyUpdate = canApplyUpdate,
                onThemeToggle = { scope.launch { themePreferences.setThemeMode(themeMode.next()) } },
                onThemeModeSelect = { mode -> scope.launch { themePreferences.setThemeMode(mode) } },
                onAboutOpen = { showAbout = !showAbout; if (showAbout) showSettings = false },
                onAboutClose = { showAbout = false },
                onSettingsOpen = { showSettings = !showSettings; if (showSettings) showAbout = false },
                onSettingsClose = { showSettings = false },
                onUpdateCheckChange = { enabled ->
                    scope.launch {
                        appUpdatePreferences.setCheckInterval(
                            SettingsLogic.intervalForToggle(enabled, checkInterval),
                        )
                    }
                },
                onApplyUpdate = {
                    val asset = applyAsset ?: return@AetherFeedScreen
                    val host = activity ?: return@AetherFeedScreen
                    scope.launch {
                        UpdateApplyCoordinator.applySideloadUpdate(host, appUpdatePreferences, asset)
                    }
                },
            )
        }
    }
}

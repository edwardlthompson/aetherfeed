package org.aetherfeed.desktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import javax.swing.ImageIcon
import org.aetherfeed.desktop.notify.DesktopNotificationPoster
import org.aetherfeed.desktop.sync.StubLanHubHost
import org.aetherfeed.desktop.ui.DesktopApp
import org.aetherfeed.desktop.ui.theme.AetherFeedTheme
import org.aetherfeed.desktop.ui.theme.ThemeMode
import org.aetherfeed.desktop.unread.formatUnreadTrayLabel
import org.aetherfeed.desktop.vault.DesktopVaultKeychain

private const val WINDOW_WIDTH = 1100
private const val WINDOW_HEIGHT = 760

fun main() {
    val keychain = DesktopVaultKeychain()
    keychain.storeSecret("aetherfeed.vault.stub", byteArrayOf(0))
    val lanHub = StubLanHubHost()

    application {
        var themeMode by remember { mutableStateOf(ThemeMode.System) }
        var unreadTotal by remember { mutableIntStateOf(0) }
        val tray = remember { createTray { formatUnreadTrayLabel(unreadTotal) } }
        val notificationPoster = remember { DesktopNotificationPoster(tray) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "AetherFeed",
            state = rememberWindowState(width = WINDOW_WIDTH.dp, height = WINDOW_HEIGHT.dp),
        ) {
            tray?.toolTip = formatUnreadTrayLabel(unreadTotal)
            AetherFeedTheme(themeMode = themeMode) {
                DesktopApp(
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it },
                    onUnreadTotalChange = {
                        unreadTotal = it
                        tray?.toolTip = formatUnreadTrayLabel(it)
                    },
                    lanHub = lanHub,
                    version = "0.1.0",
                    notificationPoster = notificationPoster,
                )
            }
        }
    }
}

private fun createTray(tooltip: () -> String): TrayIcon? {
    if (!SystemTray.isSupported()) return null
    return runCatching {
        val tray = SystemTray.getSystemTray()
        val image = ImageIcon().image
        val icon = TrayIcon(image, tooltip())
        icon.isImageAutoSize = true
        val menu = PopupMenu()
        menu.add(MenuItem("Show AetherFeed")).apply {
            addActionListener { /* Window focus row 2 */ }
        }
        menu.add(MenuItem("Quit")).apply {
            addActionListener { kotlin.system.exitProcess(0) }
        }
        icon.popupMenu = menu
        tray.add(icon)
        icon
    }.getOrNull()
}
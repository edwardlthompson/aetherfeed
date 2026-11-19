package org.aetherfeed.desktop.notify

import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import org.aetherfeed.shared.domain.notify.ArticleNotice

/** Best-effort desktop notify — uses tray balloon when supported, else logs-only stub. */
class DesktopNotificationPoster(
    private val trayIcon: TrayIcon? = null,
) {
    fun post(notice: ArticleNotice) {
        val icon = trayIcon
        if (icon != null && SystemTray.isSupported()) {
            runCatching {
                icon.displayMessage(notice.title, notice.body, TrayIcon.MessageType.INFO)
            }
        } else {
            println("[AetherFeed] ${notice.title}: ${notice.body}")
        }
    }
}

fun desktopAwtToolkit(): Toolkit? = runCatching { Toolkit.getDefaultToolkit() }.getOrNull()
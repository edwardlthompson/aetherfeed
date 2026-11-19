package org.aetherfeed.ios

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.aetherfeed.ios.ui.IosAppShell
import org.aetherfeed.ios.vault.createIosVaultKeychain

/** JVM preview of the iOS CMP shell — compiles on Windows without Xcode. */
fun main() = application {
    createIosVaultKeychain().storeSecret("vault_passphrase", byteArrayOf(1))
    Window(onCloseRequest = ::exitApplication, title = "AetherFeed iOS (JVM stub)") {
        IosAppShell(unreadTotal = 0)
    }
}
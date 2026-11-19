package org.aetherfeed.ios.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.aetherfeed.shared.domain.ModuleKind

/**
 * Shared Compose shell stub — clones Android chrome per DESIGN_BRIEF §3 (bottom bar, FAB Add-feed).
 * Full wiring lands when `shared/ui` CMP cutover is safe; iOS must not use top TabView.
 */
@Composable
fun IosAppShell(
    unreadTotal: Int = 0,
    onAddFeed: (url: String, title: String, kind: ModuleKind) -> Unit = { _, _, _ -> },
) {
    var showAdd by remember { mutableStateOf(false) }
    var tab by remember { mutableIntStateOf(0) }
    MaterialTheme {
        Surface {
            when {
                showAdd ->
                    IosAddFeedDialog(
                        onDismiss = { showAdd = false },
                        onConfirm = { url, title, kind ->
                            onAddFeed(url, title, kind)
                            showAdd = false
                        },
                    )
                else ->
                    Text(
                        "AetherFeed iOS stub — tab=$tab unread=$unreadTotal (News|Podcasts|Booru + FAB)",
                    )
            }
        }
    }
}
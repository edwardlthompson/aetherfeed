package org.aetherfeed.app.ui.news

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import org.aetherfeed.app.news.applyNewsRefresh
import org.aetherfeed.app.news.findActivityWindow
import org.aetherfeed.app.news.newsDisplayModes
import org.aetherfeed.app.news.pickNewsRefreshMode

@Composable
internal fun NewsHighRefresh(active: Boolean) {
    val view = LocalView.current
    DisposableEffect(active, view) {
        val window = view.findActivityWindow()
        val display = view.display
        val mode = display?.let { host ->
            val current = host.mode
            pickNewsRefreshMode(newsDisplayModes(host), current.physicalWidth, current.physicalHeight)
        }
        applyNewsRefresh(window, mode, active)
        onDispose { applyNewsRefresh(window, mode, false) }
    }
}

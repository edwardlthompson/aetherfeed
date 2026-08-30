package org.aetherfeed.app.ui.news

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt
import org.aetherfeed.app.news.NewsArticleSwipe
import org.aetherfeed.app.news.NewsDrawerChrome
import org.aetherfeed.app.news.newsDrawerPainted
import org.aetherfeed.app.news.newsDrawerSettleOpen

@Composable
internal fun NewsNarrowChrome(
    modifier: Modifier = Modifier,
    drawer: @Composable (close: () -> Unit) -> Unit,
    content: @Composable (open: () -> Unit) -> Unit,
) {
    val density = LocalDensity.current
    val widthPx = with(density) { NewsDrawerChrome.WIDTH_DP.dp.toPx() }
    val edgePx = with(density) { NewsArticleSwipe.EDGE_RESERVE_DP.dp.toPx() }
    var open by remember { mutableStateOf(false) }
    var dragPx by remember { mutableFloatStateOf(0f) }
    val close: () -> Unit = { open = false; dragPx = 0f }
    val show = { open = true; dragPx = widthPx }
    BackHandler(enabled = open) { close() }
    Box(
        modifier
            .fillMaxSize()
            .pointerInput(open, widthPx, edgePx) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (open || down.position.x > edgePx) return@awaitEachGesture
                    var total = 0f
                    var locked: Boolean? = null
                    drag(down.id) { change ->
                        val delta = change.positionChange()
                        if (locked == null && (abs(delta.x) > 8f || abs(delta.y) > 8f)) {
                            locked = abs(delta.x) > abs(delta.y)
                        }
                        if (locked == true) {
                            change.consume()
                            total = (total + delta.x).coerceIn(0f, widthPx)
                            dragPx = total
                        }
                    }
                    if (newsDrawerSettleOpen(total, widthPx)) {
                        open = true
                        dragPx = widthPx
                    } else {
                        close()
                    }
                }
            },
    ) {
        content(show)
        if (newsDrawerPainted(open, dragPx)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(onClick = close),
            )
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(NewsDrawerChrome.WIDTH_DP.dp)
                    .offset { IntOffset((dragPx - widthPx).roundToInt(), 0) }
                    .clipToBounds(),
            ) {
                drawer(close)
            }
        }
    }
}

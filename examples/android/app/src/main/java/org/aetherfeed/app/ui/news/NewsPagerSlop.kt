package org.aetherfeed.app.ui.news

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import org.aetherfeed.app.news.NewsArticleSwipe
import org.aetherfeed.app.news.newsPagerAxis
import org.aetherfeed.app.news.newsPagerPreScrollX

internal class NewsPagerSlop(
    private val edgeReservePx: Float,
    private val slopPx: Float,
    private val minFlingPx: Float = NewsArticleSwipe.MIN_VELOCITY_PX,
) {
    var startX: Float = Float.MAX_VALUE
    var eaten: Float = 0f
    var traveledY: Float = 0f
    var axis: Int = 0
    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            traveledY += available.y
            if (axis == 0) axis = newsPagerAxis(eaten + available.x, traveledY, 16f)
            val steal = newsPagerPreScrollX(axis, startX, eaten, available.x, edgeReservePx, slopPx)
            eaten += steal
            return Offset(steal, 0f)
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (axis < 0 || startX < edgeReservePx || abs(available.x) < minFlingPx) return available
            return Velocity.Zero
        }
    }

    fun onPointer(event: PointerEvent) {
        val change = event.changes.firstOrNull() ?: return
        if (change.changedToDown()) {
            startX = change.position.x
            eaten = 0f
            traveledY = 0f
            axis = 0
        }
    }
}

@Composable
internal fun rememberNewsPagerSlop(edgeReservePx: Float, slopPx: Float): NewsPagerSlop =
    remember(edgeReservePx, slopPx) { NewsPagerSlop(edgeReservePx, slopPx) }

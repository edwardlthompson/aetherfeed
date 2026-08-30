package org.aetherfeed.app.news

import kotlin.math.abs

object NewsArticleSwipe {
    const val EDGE_RESERVE_DP = 48f
    const val MIN_DISTANCE_DP = 112f
    const val SNAP_FRACTION = 0.72f
    const val MIN_VELOCITY_PX = 2400f
    const val BEYOND_VIEWPORT = 1
}

fun articleSwipePageDelta(
    startX: Float,
    dx: Float,
    velocityX: Float,
    pageWidth: Float,
    edgeReservePx: Float,
    slopPx: Float,
    snapFraction: Float = NewsArticleSwipe.SNAP_FRACTION,
    minVelocityPx: Float = NewsArticleSwipe.MIN_VELOCITY_PX,
): Int {
    if (startX < edgeReservePx) return 0
    if (abs(dx) < slopPx) return 0
    val next = if (dx < 0f) 1 else -1
    if (abs(velocityX) >= minVelocityPx) return next
    val width = pageWidth.coerceAtLeast(1f)
    if (abs(dx) >= width * snapFraction) return next
    return 0
}

fun newsPagerConsumeX(
    startX: Float,
    accumulated: Float,
    proposedX: Float,
    edgeReservePx: Float,
    slopPx: Float,
): Float {
    if (proposedX == 0f) return 0f
    if (startX < edgeReservePx) return proposedX
    val traveled = abs(accumulated)
    if (traveled >= slopPx) return 0f
    val remain = slopPx - traveled
    return proposedX.coerceIn(-remain, remain)
}

/** 0 unknown, 1 horizontal pager, -1 vertical reader. */
fun newsPagerAxis(dx: Float, dy: Float, axisSlopPx: Float): Int {
    if (maxOf(abs(dx), abs(dy)) < axisSlopPx) return 0
    return if (abs(dy) >= abs(dx)) -1 else 1
}

fun newsPagerPreScrollX(
    axis: Int,
    startX: Float,
    eaten: Float,
    proposedX: Float,
    edgeReservePx: Float,
    slopPx: Float,
): Float {
    if (axis < 0) return proposedX
    return newsPagerConsumeX(startX, eaten, proposedX, edgeReservePx, slopPx)
}

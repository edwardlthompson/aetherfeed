package org.aetherfeed.app.news

object NewsDrawerChrome {
    const val WIDTH_DP = 304f
    const val SETTLE_FRACTION = 0.35f
    const val PAINT_EPSILON_PX = 0.5f
}

fun newsDrawerPainted(
    open: Boolean,
    dragPx: Float,
    epsilon: Float = NewsDrawerChrome.PAINT_EPSILON_PX,
): Boolean = open || dragPx > epsilon

fun newsDrawerSettleOpen(
    dragPx: Float,
    widthPx: Float,
    fraction: Float = NewsDrawerChrome.SETTLE_FRACTION,
): Boolean {
    if (widthPx <= 0f) return false
    return dragPx >= widthPx * fraction
}

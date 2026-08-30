package org.aetherfeed.app.news

import android.app.Activity
import android.content.ContextWrapper
import android.view.Display
import android.view.Surface
import android.view.View
import android.view.Window

data class NewsDisplayMode(val id: Int, val width: Int, val height: Int, val hz: Float)

fun pickNewsRefreshMode(
    modes: List<NewsDisplayMode>,
    width: Int,
    height: Int,
): NewsDisplayMode? {
    if (modes.isEmpty()) return null
    val same = modes.filter { it.width == width && it.height == height }
    return (same.ifEmpty { modes }).maxByOrNull { it.hz }
}

fun newsRefreshHz(high: Boolean, pickedHz: Float): Float =
    if (high) pickedHz.coerceAtLeast(0f) else 0f

fun newsRefreshModeId(high: Boolean, pickedId: Int): Int = if (high) pickedId else 0

fun newsDisplayModes(display: Display): List<NewsDisplayMode> =
    display.supportedModes.map { mode ->
        NewsDisplayMode(mode.modeId, mode.physicalWidth, mode.physicalHeight, mode.refreshRate)
    }

fun View.findActivityWindow(): Window? {
    var ctx = context
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx.window
        ctx = ctx.baseContext
    }
    return null
}

fun applyNewsRefresh(window: Window?, mode: NewsDisplayMode?, high: Boolean) {
    if (window == null) return
    val hz = newsRefreshHz(high, mode?.hz ?: 0f)
    val modeId = newsRefreshModeId(high, mode?.id ?: 0)
    invokeWindowFrameRate(window, hz)
    val lp = window.attributes
    lp.preferredRefreshRate = hz
    lp.preferredDisplayModeId = modeId
    window.attributes = lp
}

internal fun invokeWindowFrameRate(window: Window, hz: Float) {
    try {
        val method = Window::class.java.getMethod(
            "setFrameRate",
            Float::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
        )
        method.invoke(
            window,
            hz,
            Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE,
            Surface.CHANGE_FRAME_RATE_ONLY_IF_SEAMLESS,
        )
    } catch (_: ReflectiveOperationException) {
        // Window.setFrameRate is flagged / missing on this stub.
    }
}

package org.aetherfeed.app.applock

import android.app.Activity
import android.os.Build
import android.view.WindowManager

fun applySecureRecents(activity: Activity) {
    activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        activity.setRecentsScreenshotEnabled(false)
    }
}

fun isWindowSecure(activity: Activity): Boolean =
    activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0

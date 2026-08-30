package org.aetherfeed.app.applock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.aetherfeed.app.domain.APP_LOCK_TIMEOUT_MS

@Composable
fun WatchAppLockTimeout(
    session: AppLockSession,
    unlocked: Boolean,
    onLock: () -> Unit,
) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, unlocked) {
        var hiddenAt = 0L
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> if (unlocked) hiddenAt = System.currentTimeMillis()
                Lifecycle.Event.ON_START -> {
                    if (unlocked && hiddenAt > 0 && System.currentTimeMillis() - hiddenAt >= APP_LOCK_TIMEOUT_MS) {
                        session.lock()
                        onLock()
                    }
                }
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}

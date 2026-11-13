package org.aetherfeed.app.sync

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedChangeNotifier
@Inject
constructor() {
    @Volatile
    var listener: (suspend () -> Unit)? = null

    @Volatile
    private var suppressed: Boolean = false

    suspend fun notifyChanged() {
        if (suppressed) {
            return
        }
        runCatching { listener?.invoke() }
    }

    suspend fun <T> suppress(block: suspend () -> T): T {
        suppressed = true
        try {
            return block()
        } finally {
            suppressed = false
        }
    }
}

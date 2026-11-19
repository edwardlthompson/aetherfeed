package org.aetherfeed.app.notify

import android.content.Context
import org.unifiedpush.android.connector.UnifiedPush

/** FOSS push wake — distributor API only (no FCM / Play Services). */
object UnifiedPushConnector {
    fun registerIfNeeded(context: Context) {
        runCatching { UnifiedPush.registerApp(context) }
    }
}
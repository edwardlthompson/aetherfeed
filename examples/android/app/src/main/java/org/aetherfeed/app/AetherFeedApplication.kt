package org.aetherfeed.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.aetherfeed.app.notify.NotificationChannelManager

@HiltAndroidApp
class AetherFeedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannelManager(this).ensureChannels(emptyList())
    }
}

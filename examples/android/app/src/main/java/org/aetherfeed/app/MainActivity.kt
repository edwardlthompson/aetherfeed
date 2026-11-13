package org.aetherfeed.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import org.aetherfeed.app.BuildConfig
import org.aetherfeed.app.about.AppUpdatePreferences
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.network.NetworkStatusMonitor
import org.aetherfeed.app.news.NewsFeedRefresh
import org.aetherfeed.app.news.ReaderPrefs
import org.aetherfeed.app.news.ReaderSettings
import org.aetherfeed.app.sync.FeedChangeNotifier
import org.aetherfeed.app.sync.LanPoolConfig
import org.aetherfeed.app.sync.LanPoolPrefs
import org.aetherfeed.app.sync.LanPoolSync
import org.aetherfeed.app.sync.ReadReceiptSync
import org.aetherfeed.app.sync.SubscriptionPoolSync
import org.aetherfeed.app.ui.AetherFeedApp
import org.aetherfeed.app.ui.theme.ThemePreferences

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_LAN_HUB = "lan_hub"
        const val EXTRA_LAN_PASS = "lan_pass"
    }
    @Inject
    lateinit var libraryRepository: LibraryRepository

    @Inject
    lateinit var subscriptionPoolSync: SubscriptionPoolSync

    @Inject
    lateinit var lanPoolSync: LanPoolSync

    @Inject
    lateinit var lanPoolPrefs: LanPoolPrefs

    @Inject
    lateinit var readerPrefs: ReaderPrefs

    @Inject
    lateinit var readReceiptSync: ReadReceiptSync

    @Inject
    lateinit var feedChangeNotifier: FeedChangeNotifier

    private var networkStatusMonitor: NetworkStatusMonitor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        libraryRepository.hashCode()
        feedChangeNotifier.listener = { subscriptionPoolSync.publishFromLocal() }
        val themePreferences = ThemePreferences(applicationContext)
        val appUpdatePreferences = AppUpdatePreferences(applicationContext)
        networkStatusMonitor = NetworkStatusMonitor(applicationContext).also { it.start() }

        lifecycleScope.launch {
            appUpdatePreferences.clearPendingRestart()
            appUpdatePreferences.ensureInstalledFormat()
            if (BuildConfig.DEBUG) {
                val hub = intent.getStringExtra(EXTRA_LAN_HUB).orEmpty()
                val pass = intent.getStringExtra(EXTRA_LAN_PASS).orEmpty()
                if (hub.isNotBlank() && pass.isNotBlank()) {
                    lanPoolPrefs.save(LanPoolConfig(hubUrl = hub, passphrase = pass))
                }
            }
            subscriptionPoolSync.pullAndMerge()
            lanPoolSync.syncNow()
            NewsFeedRefresh.refresh(libraryRepository)
            // After ingest: apply Hermes read receipts (initial unread stamps are epoch).
            readReceiptSync.pullAndApply()
            lanPoolSync.syncNow()
        }

        setContent {
            val poolStatus by subscriptionPoolSync.status.collectAsStateWithLifecycle()
            val lanStatus by lanPoolSync.status.collectAsStateWithLifecycle()
            val readerSettings by readerPrefs.settings.collectAsStateWithLifecycle(
                initialValue = ReaderSettings(),
            )
            var lanConfig by remember { mutableStateOf(LanPoolConfig()) }
            LaunchedEffect(Unit) {
                lanConfig = lanPoolPrefs.load()
            }
            AetherFeedApp(
                context = this,
                scope = lifecycleScope,
                themePreferences = themePreferences,
                appUpdatePreferences = appUpdatePreferences,
                networkStatusMonitor = networkStatusMonitor!!,
                hermesPoolStatus = poolStatus.message,
                lanPoolStatus = lanStatus.message,
                lanHubUrl = lanConfig.hubUrl,
                lanPassphrase = lanConfig.passphrase,
                onLanSave = { hub, pass ->
                    lifecycleScope.launch {
                        val next = LanPoolConfig(hubUrl = hub, passphrase = pass)
                        lanPoolPrefs.save(next)
                        lanConfig = next
                        lanPoolSync.syncNow()
                    }
                },
                onLanSync = { lifecycleScope.launch { lanPoolSync.syncNow() } },
                readerSettings = readerSettings,
                onReaderTarget = { value -> lifecycleScope.launch { readerPrefs.setTarget(value) } },
                onReaderScale = { value -> lifecycleScope.launch { readerPrefs.setBodyScale(value) } },
                onReaderImages = { value -> lifecycleScope.launch { readerPrefs.setImagePolicy(value) } },
                libraryRepository = libraryRepository,
                readReceiptSync = readReceiptSync,
                subscriptionPoolSync = subscriptionPoolSync,
                lanPoolSync = lanPoolSync,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            subscriptionPoolSync.pullAndMerge()
            lanPoolSync.syncNow()
            NewsFeedRefresh.refresh(libraryRepository)
            readReceiptSync.pullAndApply()
            lanPoolSync.syncNow()
        }
    }

    override fun onDestroy() {
        networkStatusMonitor?.stop()
        super.onDestroy()
    }
}

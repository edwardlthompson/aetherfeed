package org.aetherfeed.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.aetherfeed.app.R
import org.aetherfeed.app.news.FeedRefreshPrefs
import org.aetherfeed.app.news.FeedRefreshScheduler
import org.aetherfeed.app.news.HISTORY_COUNT_CHOICES
import org.aetherfeed.app.news.HISTORY_DAYS_CHOICES
import org.aetherfeed.app.news.HistoryMode
import org.aetherfeed.app.news.HistoryPolicy
import org.aetherfeed.app.news.REFRESH_INTERVAL_DEFAULT
import org.aetherfeed.app.news.REFRESH_INTERVAL_HOURS
import org.aetherfeed.app.ui.theme.SpacingMd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewsRefreshSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { FeedRefreshPrefs(context) }
    val interval by prefs.intervalHours.collectAsState(REFRESH_INTERVAL_DEFAULT)
    val policy by prefs.policy.collectAsState(HistoryPolicy())
    val wifiOnly by prefs.wifiOnly.collectAsState(true)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(stringResource(R.string.settings_refresh_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_refresh_hint), style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            REFRESH_INTERVAL_HOURS.forEach { hours ->
                FilterChip(
                    selected = interval == hours,
                    onClick = {
                        scope.launch {
                            prefs.setIntervalHours(hours)
                            FeedRefreshScheduler.updateInterval(context, hours)
                        }
                    },
                    label = { Text(stringResource(R.string.settings_refresh_hours, hours)) },
                    modifier = Modifier.testTag("refresh-interval-$hours"),
                )
            }
        }
        Text(stringResource(R.string.settings_network_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_network_hint), style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            FilterChip(
                selected = wifiOnly,
                onClick = { scope.launch { prefs.setWifiOnly(true) } },
                label = { Text(stringResource(R.string.settings_wifi_only)) },
                modifier = Modifier.testTag("news-wifi-only"),
            )
            FilterChip(
                selected = !wifiOnly,
                onClick = { scope.launch { prefs.setWifiOnly(false) } },
                label = { Text(stringResource(R.string.settings_allow_cellular)) },
                modifier = Modifier.testTag("news-allow-cellular"),
            )
        }
        Text(stringResource(R.string.settings_history_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_history_hint), style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            FilterChip(
                selected = policy.mode == HistoryMode.Count,
                onClick = { scope.launch { prefs.setHistoryMode(HistoryMode.Count) } },
                label = { Text(stringResource(R.string.settings_history_mode_count)) },
                modifier = Modifier.testTag("history-mode-count"),
            )
            FilterChip(
                selected = policy.mode == HistoryMode.Days,
                onClick = { scope.launch { prefs.setHistoryMode(HistoryMode.Days) } },
                label = { Text(stringResource(R.string.settings_history_mode_days)) },
                modifier = Modifier.testTag("history-mode-days"),
            )
        }
        if (policy.mode == HistoryMode.Count) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                HISTORY_COUNT_CHOICES.forEach { count ->
                    FilterChip(
                        selected = policy.count == count,
                        onClick = { scope.launch { prefs.setHistoryCount(count) } },
                        label = { Text(stringResource(R.string.settings_history_count, count)) },
                        modifier = Modifier.testTag("history-count-$count"),
                    )
                }
            }
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                HISTORY_DAYS_CHOICES.forEach { days ->
                    FilterChip(
                        selected = policy.days == days,
                        onClick = { scope.launch { prefs.setHistoryDays(days) } },
                        label = { Text(stringResource(R.string.settings_history_days, days)) },
                        modifier = Modifier.testTag("history-days-$days"),
                    )
                }
            }
        }
    }
}

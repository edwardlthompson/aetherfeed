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
import org.aetherfeed.app.news.CacheRetainMode
import org.aetherfeed.app.news.CacheRetainPrefs
import org.aetherfeed.app.ui.theme.SpacingMd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CacheRetainSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { CacheRetainPrefs(context) }
    val mode by prefs.state.collectAsState(CacheRetainMode.Days30)
    val scope = rememberCoroutineScope()
    Column(modifier = modifier.testTag("cache-retain"), verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(stringResource(R.string.settings_cache_retain_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_cache_retain_hint), style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            FilterChip(
                selected = mode == CacheRetainMode.Days30,
                onClick = { scope.launch { prefs.setMode(CacheRetainMode.Days30) } },
                label = { Text(stringResource(R.string.settings_cache_retain_days)) },
            )
            FilterChip(
                selected = mode == CacheRetainMode.NextSync,
                onClick = { scope.launch { prefs.setMode(CacheRetainMode.NextSync) } },
                label = { Text(stringResource(R.string.settings_cache_retain_sync)) },
            )
        }
    }
}

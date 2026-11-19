package org.aetherfeed.shared.ui.podcasts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.aetherfeed.shared.ui.theme.SpacingSm

/**
 * Compact podcast player bar (DESIGN_BRIEF §4). Wire to Media3/AVPlayer actuals in later sprints.
 * Accessible names come from callers until shared string resources land with chrome port.
 */
@Composable
fun PodcastPlayerBar(
    playing: Boolean,
    positionMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    playPauseContentDescription: String,
    modifier: Modifier = Modifier,
) {
    val duration = durationMs.coerceAtLeast(1L)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(SpacingSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingSm),
    ) {
        IconButton(onClick = onPlayPause) {
            Icon(
                imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = playPauseContentDescription,
            )
        }
        Slider(
            value = (positionMs.toFloat() / duration.toFloat()).coerceIn(0f, 1f),
            onValueChange = { onSeek((it * duration).toLong()) },
            modifier = Modifier.weight(1f),
        )
    }
}

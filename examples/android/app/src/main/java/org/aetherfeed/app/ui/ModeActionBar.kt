package org.aetherfeed.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Drafts
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.ui.navigation.AppDestination

@Composable
fun ModeActionBar(
    destination: AppDestination,
    bus: ModeActionBus,
    modifier: Modifier = Modifier,
) {
    bus.revision
    Surface(modifier = modifier.fillMaxWidth().testTag("action-bar")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShareActionButton(bus)
            when (destination) {
                AppDestination.News -> {
                    IconButton(
                        onClick = { bus.prev?.invoke() },
                        enabled = bus.prev != null,
                        modifier = Modifier.testTag("action-prev"),
                    ) {
                        Icon(Icons.Outlined.SkipPrevious, contentDescription = stringResource(R.string.action_prev))
                    }
                    IconButton(
                        onClick = { bus.star?.invoke() },
                        enabled = bus.star != null,
                        modifier = Modifier.testTag("action-star"),
                    ) {
                        Icon(
                            if (bus.starred) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                            contentDescription = stringResource(
                                if (bus.starred) R.string.news_unstar else R.string.news_star,
                            ),
                        )
                    }
                    IconButton(
                        onClick = { bus.toggleRead?.invoke() },
                        enabled = bus.toggleRead != null,
                        modifier = Modifier.testTag("action-unread"),
                    ) {
                        Icon(
                            if (bus.isRead) Icons.Outlined.Drafts else Icons.Outlined.MarkEmailRead,
                            contentDescription = stringResource(
                                if (bus.isRead) R.string.news_mark_unread else R.string.news_mark_read,
                            ),
                        )
                    }
                    IconButton(
                        onClick = { bus.next?.invoke() },
                        enabled = bus.next != null,
                        modifier = Modifier.testTag("action-next"),
                    ) {
                        Icon(Icons.Outlined.SkipNext, contentDescription = stringResource(R.string.action_next))
                    }
                }
                AppDestination.Podcasts -> {
                    IconButton(
                        onClick = { bus.skipBack?.invoke() },
                        enabled = bus.skipBack != null,
                        modifier = Modifier.testTag("action-skip-back"),
                    ) {
                        Icon(Icons.Outlined.SkipPrevious, contentDescription = stringResource(R.string.action_skip_back))
                    }
                    IconButton(
                        onClick = { bus.play?.invoke() },
                        enabled = bus.play != null,
                        modifier = Modifier.testTag("action-play"),
                    ) {
                        Icon(
                            if (bus.playing) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                            contentDescription = stringResource(
                                if (bus.playing) R.string.action_pause else R.string.action_play,
                            ),
                        )
                    }
                    IconButton(
                        onClick = { bus.skipFwd?.invoke() },
                        enabled = bus.skipFwd != null,
                        modifier = Modifier.testTag("action-skip-fwd"),
                    ) {
                        Icon(Icons.Outlined.SkipNext, contentDescription = stringResource(R.string.action_skip_fwd))
                    }
                }
                AppDestination.Booru -> {
                    IconButton(
                        onClick = { bus.favorite?.invoke() },
                        enabled = bus.favorite != null,
                        modifier = Modifier.testTag("action-favorite"),
                    ) {
                        Icon(Icons.Outlined.Favorite, contentDescription = stringResource(R.string.action_favorite))
                    }
                    IconButton(
                        onClick = { bus.info?.invoke() },
                        enabled = bus.info != null,
                        modifier = Modifier.testTag("action-info"),
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.action_info))
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareActionButton(bus: ModeActionBus) {
    IconButton(
        onClick = { bus.share?.invoke() },
        enabled = bus.share != null,
        modifier = Modifier.testTag("action-share"),
    ) {
        Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.action_share))
    }
}

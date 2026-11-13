package org.aetherfeed.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R
import org.aetherfeed.app.news.ArticleTarget
import org.aetherfeed.app.news.ImagePolicy
import org.aetherfeed.app.news.ReaderScale
import org.aetherfeed.app.news.ReaderSettings
import org.aetherfeed.app.ui.theme.SpacingMd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsReaderSection(
    settings: ReaderSettings,
    onTarget: (ArticleTarget) -> Unit,
    onScale: (Float) -> Unit,
    onImages: (ImagePolicy) -> Unit,
) {
    Text(
        text = stringResource(R.string.feed_opener_title),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.testTag("settings-article-opener"),
    )
    Text(text = stringResource(R.string.feed_opener_body), style = MaterialTheme.typography.bodySmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
        ArticleTarget.entries.forEach { target ->
            FilterChip(
                selected = settings.target == target,
                onClick = { onTarget(target) },
                label = { Text(stringResource(openerLabel(target))) },
                modifier = Modifier.testTag("settings-opener-${target.name.lowercase()}"),
            )
        }
    }
    Text(text = stringResource(R.string.feed_reader_scale), style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
        listOf(ReaderScale.MIN, 1.0f, 1.15f, 1.3f, 1.45f, ReaderScale.MAX).forEach { scale ->
            FilterChip(
                selected = kotlin.math.abs(settings.bodyScale - scale) < 0.01f,
                onClick = { onScale(scale) },
                label = {
                    Text(text = stringResource(R.string.feed_reader_scale_percent, (scale * 100).toInt()))
                },
            )
        }
    }
    Text(text = stringResource(R.string.feed_images_title), style = MaterialTheme.typography.titleSmall)
    Text(text = stringResource(R.string.feed_images_body), style = MaterialTheme.typography.bodySmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
        ImagePolicy.entries.forEach { policy ->
            FilterChip(
                selected = settings.imagePolicy == policy,
                onClick = { onImages(policy) },
                label = { Text(stringResource(imageLabel(policy))) },
                modifier = Modifier.testTag("settings-images-${policy.name.lowercase()}"),
            )
        }
    }
}

private fun openerLabel(target: ArticleTarget): Int =
    when (target) {
        ArticleTarget.LAUNCHER -> R.string.feed_opener_launcher
        ArticleTarget.BROWSER -> R.string.feed_opener_browser
        ArticleTarget.CUSTOM_TAB -> R.string.feed_opener_custom_tab
    }

private fun imageLabel(policy: ImagePolicy): Int =
    when (policy) {
        ImagePolicy.ALWAYS -> R.string.feed_images_always
        ImagePolicy.WIFI -> R.string.feed_images_wifi
        ImagePolicy.NEVER -> R.string.feed_images_never
    }

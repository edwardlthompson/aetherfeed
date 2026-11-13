package org.aetherfeed.app.ui.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private const val SWIPE_PX = 96f

fun Modifier.horizontalArticleSwipe(
    onPrev: (() -> Unit)?,
    onNext: (() -> Unit)?,
): Modifier =
    pointerInput(onPrev, onNext) {
        var total = 0f
        detectHorizontalDragGestures(
            onDragCancel = { total = 0f },
            onDragEnd = {
                when {
                    total > SWIPE_PX -> onPrev?.invoke()
                    total < -SWIPE_PX -> onNext?.invoke()
                }
                total = 0f
            },
            onHorizontalDrag = { _, amount -> total += amount },
        )
    }

@Composable
fun ArticleEdgeTaps(
    prevLabel: String,
    nextLabel: String,
    onPrev: (() -> Unit)?,
    onNext: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        if (onPrev != null) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(48.dp)
                        .testTag("article-prev")
                        .semantics { contentDescription = prevLabel }
                        .clickable(onClick = onPrev),
            )
        }
        if (onNext != null) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(48.dp)
                        .testTag("article-next")
                        .semantics { contentDescription = nextLabel }
                        .clickable(onClick = onNext),
            )
        }
    }
}

package org.aetherfeed.shared.ui.news

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.aetherfeed.shared.domain.Article
import org.aetherfeed.shared.ui.theme.SpacingMd

/** Shared reader chrome + body slot. WebView / fetch stay platform-side (Sprint 58). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleReaderChrome(
    article: Article,
    starred: Boolean,
    backCd: String,
    starCd: String,
    onBack: () -> Unit,
    onToggleStar: () -> Unit,
    body: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().testTag("article-reader")) {
        TopAppBar(
            title = { Text(article.title, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("article-reader-back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backCd)
                }
            },
            actions = {
                IconButton(onClick = onToggleStar, modifier = Modifier.testTag("article-star")) {
                    Icon(
                        imageVector = if (starred) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = starCd,
                    )
                }
            },
        )
        body()
    }
}

@Composable
fun ArticleReaderPlainBody(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SpacingMd)
                .testTag("article-reader-body"),
    )
}

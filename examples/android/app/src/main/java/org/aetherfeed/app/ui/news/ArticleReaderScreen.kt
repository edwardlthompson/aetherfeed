package org.aetherfeed.app.ui.news

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.aetherfeed.app.R
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.news.ArticleFetch
import org.aetherfeed.app.news.ArticleOpen
import org.aetherfeed.app.news.ReaderMode
import org.aetherfeed.app.news.ReaderSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleReaderScreen(
    article: Article,
    settings: ReaderSettings,
    isOnline: Boolean,
    onBack: () -> Unit,
    onPrev: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    starred: Boolean = false,
    onToggleStar: () -> Unit = {},
    onParkUnread: () -> Unit = {},
    initialMode: ReaderMode = ReaderMode.READING,
    onModeChange: (ReaderMode) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var modeName by rememberSaveable(initialMode.name) { mutableStateOf(initialMode.name) }
    val mode = ReaderMode.entries.firstOrNull { it.name == modeName } ?: ReaderMode.READING
    var pageHtml by remember(article.id) { mutableStateOf<String?>(null) }
    val wifi = remember { context.isUnmeteredWifi() }
    val showImages = settings.imagePolicy.allowImages(isOnline, wifi)
    LaunchedEffect(article.id, mode) {
        if (mode == ReaderMode.WEB) return@LaunchedEffect
        if (pageHtml == null) {
            pageHtml = withContext(Dispatchers.IO) { ArticleFetch.html(article.url) }
        }
    }
    BackHandler(onBack = onBack)
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("article-reader")
                .horizontalArticleSwipe(onPrev = onPrev, onNext = onNext),
    ) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            windowInsets = WindowInsets.statusBars,
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            title = { Text(article.title, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("article-reader-back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back),
                    )
                }
            },
            actions = {
                val starLabel =
                    stringResource(
                        if (starred) R.string.feed_reader_unstar else R.string.feed_reader_star,
                    )
                ReaderTooltipIcon(
                    label = starLabel,
                    testTag = "article-star",
                    onClick = onToggleStar,
                ) {
                    Icon(
                        imageVector = if (starred) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = starLabel,
                    )
                }
                val parkLabel = stringResource(R.string.feed_reader_park_unread)
                ReaderTooltipIcon(
                    label = parkLabel,
                    testTag = "article-park-unread",
                    onClick = onParkUnread,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = parkLabel,
                    )
                }
                val browserLabel = stringResource(R.string.feed_reader_browser)
                ReaderTooltipIcon(
                    label = browserLabel,
                    testTag = "article-open-browser",
                    onClick = { ArticleOpen.openBrowser(context, article.url) },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = browserLabel,
                    )
                }
                ReaderMode.entries.forEach { entry ->
                    val modeText = stringResource(modeLabel(entry))
                    ReaderTooltipIcon(
                        label = modeText,
                        testTag = "reader-mode-${entry.name.lowercase()}",
                        onClick = {
                            modeName = entry.name
                            onModeChange(entry)
                        },
                    ) {
                        Icon(
                            imageVector = modeIcon(entry),
                            contentDescription = modeText,
                            tint =
                                if (mode == entry) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                    }
                }
            },
        )
        when (mode) {
            ReaderMode.WEB ->
                ArticleWebView(url = article.url, modifier = Modifier.fillMaxWidth().weight(1f))
            else ->
                ArticleReaderBlocks(
                    article = article,
                    pageHtml = pageHtml,
                    mode = mode,
                    showImages = showImages,
                    bodyScale = settings.bodyScale,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
        }
    }
        ArticleEdgeTaps(
            prevLabel = stringResource(R.string.feed_reader_previous),
            nextLabel = stringResource(R.string.feed_reader_next),
            onPrev = onPrev,
            onNext = onNext,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTooltipIcon(
    label: String,
    testTag: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState(),
    ) {
        IconButton(onClick = onClick, modifier = Modifier.testTag(testTag), content = content)
    }
}

private fun modeIcon(mode: ReaderMode): ImageVector =
    when (mode) {
        ReaderMode.READING -> Icons.AutoMirrored.Outlined.List
        ReaderMode.FULL -> Icons.Outlined.Menu
        ReaderMode.WEB -> Icons.Outlined.Place
    }

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun ArticleWebView(
    url: String,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier.testTag("article-webview"),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.blockNetworkImage = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        update = { view -> if (view.url != url) view.loadUrl(url) },
        onRelease = { it.destroy() },
    )
}

internal fun readingSource(article: Article, pageHtml: String?, mode: ReaderMode): String {
    val page = pageHtml.orEmpty()
    val rss = article.summary.orEmpty()
    return when (mode) {
        ReaderMode.READING -> if (page.length >= 80) page else rss
        ReaderMode.FULL -> page
        ReaderMode.WEB -> ""
    }
}

private fun android.content.Context.isUnmeteredWifi(): Boolean {
    val cm = getSystemService(ConnectivityManager::class.java) ?: return false
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
}

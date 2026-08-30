package org.aetherfeed.app.ui.news

import android.view.MotionEvent
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream
import kotlin.math.abs
import org.aetherfeed.app.news.NewsArticleSwipe
import org.aetherfeed.app.news.wrapReaderDocument

@Composable
fun NewsReaderHtml(html: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val bg = cssHex(scheme.background)
    val fg = cssHex(scheme.onBackground)
    val muted = cssHex(scheme.onSurfaceVariant)
    val page = remember(html, bg, fg, muted) { wrapReaderDocument(html, bg, fg, muted) }
    var scrolling by remember { mutableStateOf(false) }
    NewsHighRefresh(scrolling)
    AndroidView(
        factory = { context ->
            var downX = 0f
            var downY = 0f
            val density = context.resources.displayMetrics.density
            val edge = NewsArticleSwipe.EDGE_RESERVE_DP * density
            val pageSlop = NewsArticleSwipe.MIN_DISTANCE_DP * density
            WebView(context).apply {
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                isNestedScrollingEnabled = false
                overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
                isVerticalScrollBarEnabled = true
                isHorizontalScrollBarEnabled = false
                settings.javaScriptEnabled = false
                settings.allowFileAccess = false
                settings.blockNetworkLoads = true
                settings.loadsImagesAutomatically = true
                settings.setSupportZoom(false)
                tag = ""
                setOnTouchListener { host, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = event.x
                            downY = event.y
                        }
                        else -> Unit
                    }
                    val dx = event.x - downX
                    val dy = event.y - downY
                    val edgeSwipe = downX < edge
                    val pageSwipe = !edgeSwipe && event.actionMasked == MotionEvent.ACTION_MOVE &&
                        abs(dx) >= pageSlop && abs(dx) > abs(dy)
                    val lock = !edgeSwipe && !pageSwipe &&
                        event.actionMasked != MotionEvent.ACTION_UP &&
                        event.actionMasked != MotionEvent.ACTION_CANCEL
                    var parent = host.parent
                    while (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(lock)
                        parent = parent.parent
                    }
                    false
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = true
                    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                        val url = request.url.toString()
                        if (url.startsWith("data:") || url.startsWith("about:")) return null
                        return emptyResponse()
                    }
                    override fun onPageFinished(view: WebView, url: String?) {
                        val n = (view.tag as? String)?.length ?: 0
                        android.util.Log.i("AetherFeed", "reader.html $n")
                    }
                }
            }
        },
        update = { view ->
            val stop = Runnable { scrolling = false }
            view.setOnScrollChangeListener { host, _, _, _, _ ->
                scrolling = true
                host.removeCallbacks(stop)
                host.postDelayed(stop, 180)
            }
            if (view.tag != page) {
                view.tag = page
                android.util.Log.i("AetherFeed", "reader.load ${page.length}")
                view.loadDataWithBaseURL("about:blank", page, "text/html", "utf-8", null)
            }
        },
        modifier = modifier.fillMaxSize().testTag("news-reader-html"),
    )
}

private fun cssHex(color: androidx.compose.ui.graphics.Color): String =
    "#%06X".format(color.toArgb() and 0xFFFFFF)

private fun emptyResponse(): WebResourceResponse =
    WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))

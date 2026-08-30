package org.aetherfeed.app.ui.news

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.news.NewsNavState
import org.aetherfeed.app.news.NewsUnreadMaps
import org.aetherfeed.app.news.newsCanGoBack
import org.aetherfeed.app.news.newsPop
import org.aetherfeed.app.ui.ModeActionBus
import org.aetherfeed.app.ui.navigation.LibraryPick
import org.aetherfeed.app.ui.navigation.parent
import org.aetherfeed.app.ui.navigation.AppDestination
import org.aetherfeed.app.ui.navigation.shareText
import org.aetherfeed.app.ui.navigation.shareUrl
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun NewsPaneChrome(
    feeds: List<Feed>,
    grouped: List<Pair<String, List<Feed>>>,
    pick: LibraryPick,
    selectedFolder: String?,
    selectedFeedId: String?,
    sorted: List<Article>,
    selectedArticle: Article?,
    starred: Set<String>,
    readIds: Set<String>,
    unreadMaps: NewsUnreadMaps,
    statusText: String,
    refreshing: Boolean,
    cacheUi: NewsCacheUi,
    feedTitleOf: (Article) -> String,
    library: LibraryTreeState,
    actions: ModeActionBus?,
    onFolder: (String) -> Unit,
    onFeed: (Feed) -> Unit,
    onRefresh: () -> Unit,
    onOpen: (Article) -> Unit,
    onStar: (Article) -> Unit,
    onToggleRead: (Article) -> Unit,
    onPick: (LibraryPick) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nav = NewsNavState(selectedFolder, selectedFeedId, selectedArticle?.id)
    BackHandler(enabled = newsCanGoBack(nav) || pick.parent() != null) {
        if (selectedArticle != null) {
            onBack()
            return@BackHandler
        }
        pick.parent()?.let(onPick)
    }
    val context = LocalContext.current
    BindNewsActions(sorted, selectedArticle, starred, readIds, actions, onOpen, onStar, onToggleRead)
    androidx.compose.runtime.DisposableEffect(selectedArticle?.url, actions) {
        if (actions != null) {
            val url = shareUrl(selectedArticle?.url, null, null, AppDestination.News)
            actions.share = url?.let { href -> { shareText(context, href) } }
            actions.publish()
        }
        onDispose { actions?.share = null }
    }
    NewsPaneFrame(
        feeds, grouped, selectedFolder, selectedFeedId, sorted, selectedArticle, starred, readIds,
        unreadMaps.byFolder, unreadMaps.byFeed,
        statusText, refreshing, cacheUi, feedTitleOf, onFolder, onFeed, onRefresh, onOpen, onStar, onToggleRead,
        { selectedArticle?.let { onBack() } ?: pick.parent()?.let(onPick) },
        library, modifier,
    )
}

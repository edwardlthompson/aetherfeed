package org.aetherfeed.app.ui.news

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.aetherfeed.app.applock.EncryptedCache
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.news.NewsRepository
import org.aetherfeed.app.news.NewsUnreadMaps
import org.aetherfeed.app.news.loadHeadlineThumbs
import org.aetherfeed.app.news.newsUnreadMaps
import org.aetherfeed.app.news.sortArticles
import org.aetherfeed.app.news.warmFeedEnds
import org.aetherfeed.app.domain.unreadByModule
import org.aetherfeed.app.ui.UnreadCounts

@Composable
internal fun NewsPaneEffects(
    cache: EncryptedCache,
    news: NewsRepository?,
    vault: LibraryRepository?,
    feeds: List<Feed>,
    articles: List<Article>,
    oldestFirst: Boolean,
    readIds: Set<String>,
    selectedFeedId: String?,
    selectedArticle: Article?,
    savedArticleId: String?,
    folderOf: (Feed) -> String,
    onThumbs: (Map<String, String>) -> Unit,
    onUnreadMaps: (NewsUnreadMaps) -> Unit,
    onUnread: (Int) -> Unit,
    onUnreadCounts: (UnreadCounts) -> Unit,
    onPrefetch: () -> Unit,
    onOpenSaved: (Article) -> Unit,
) {
    LaunchedEffect(feeds, articles, oldestFirst) {
        val thumbs = withContext(Dispatchers.IO) {
            warmFeedEnds(cache, sortArticles(articles, oldestFirst))
            loadHeadlineThumbs(cache, articles)
        }
        if (thumbs.isNotEmpty()) onThumbs(thumbs)
        if (selectedArticle == null) onPrefetch()
    }
    LaunchedEffect(feeds, articles, readIds, selectedFeedId) {
        val next = withContext(Dispatchers.IO) {
            newsUnreadMaps(news, feeds, articles, selectedFeedId, readIds, folderOf)
        }
        onUnreadMaps(next)
        onUnread(next.total)
        val byMod = unreadByModule(vault?.readStates().orEmpty())
        onUnreadCounts(
            UnreadCounts(
                news = next.total,
                podcasts = byMod[ModuleKind.Podcast] ?: 0,
                boards = byMod[ModuleKind.Booru] ?: 0,
            ),
        )
    }
    LaunchedEffect(savedArticleId, articles) {
        if (selectedArticle == null && savedArticleId != null) {
            articles.firstOrNull { it.id == savedArticleId }?.let(onOpenSaved)
        }
    }
}

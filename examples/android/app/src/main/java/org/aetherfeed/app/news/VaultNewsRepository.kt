package org.aetherfeed.app.news

import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus

class VaultNewsRepository(
    private val library: LibraryRepository,
    private val fetcher: FeedBodyFetcher,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val policy: suspend () -> HistoryPolicy = { HistoryPolicy() },
    private val index: ArticleIndexStore = NoopArticleIndex,
) : NewsRepository {
    private val cached = linkedMapOf<String, Article>()
    private val folders = linkedMapOf<String, String>()

    fun folderFor(feed: Feed): String = folders[feed.id]?.ifBlank { null } ?: feed.folderLabel()

    override suspend fun subscribe(url: String): Feed {
        val parsed = parseFeedBody(fetcher.fetch(url, DEFAULT_FEED_TIMEOUT_MS))
        val feed = Feed(
            id = "feed:$url",
            title = parsed.title.ifBlank { url },
            url = url,
            kind = ModuleKind.News,
            siteUrl = parsed.siteUrl,
            updatedAt = clock(),
        )
        library.upsertFeed(feed)
        return feed
    }

    override suspend fun updateFeed(feed: Feed) {
        library.upsertFeed(feed)
    }

    override suspend fun unsubscribe(feedId: String) {
        library.deleteFeed(feedId)
        cached.values.removeAll { it.feedId == feedId }
        folders.remove(feedId)
        index.save(feedId, emptyList())
    }

    override suspend fun importOpml(xml: String): List<Feed> {
        val imported = parseOpmlFeeds(xml, clock())
        for (row in imported) {
            library.upsertFeed(row.feed)
            if (!row.folder.isNullOrBlank()) folders[row.feed.id] = row.folder.trim()
        }
        return imported.map { it.feed }
    }

    override suspend fun exportOpml(): String =
        exportOpmlXml(library.feeds().filter { it.kind == ModuleKind.News }, ::folderFor)

    override suspend fun refresh(feedId: String): List<Article> = try {
        pullArticles(feedId)
    } catch (error: NewsRefreshError) {
        throw error
    } catch (error: CancellationException) {
        throw NewsRefreshError.Aborted(error.message ?: "Refresh aborted")
    } catch (error: SocketTimeoutException) {
        throw NewsRefreshError.Timeout(error.message ?: "Refresh timed out")
    } catch (error: InterruptedIOException) {
        throw NewsRefreshError.Timeout(error.message ?: "Refresh timed out")
    } catch (error: Exception) {
        throw NewsRefreshError.Unavailable(error.message ?: "Refresh failed")
    }

    override suspend fun articles(feedId: String?): List<Article> {
        if (feedId != null && cached.values.none { it.feedId == feedId }) {
            for (row in index.load(feedId)) cached[row.id] = row
        }
        return if (feedId == null) cached.values.toList() else cached.values.filter { it.feedId == feedId }
    }

    private suspend fun pullArticles(feedId: String): List<Article> {
        val feed = library.feeds().firstOrNull { it.id == feedId } ?: throw NewsRefreshError.UnknownFeed(feedId)
        val parsed = parseFeedBody(fetcher.fetch(feed.url, DEFAULT_FEED_TIMEOUT_MS))
        val now = clock()
        val rows = parsed.items.map { item ->
            Article(
                id = "$feedId:${item.id}",
                feedId = feedId,
                title = item.title,
                url = item.url,
                publishedAt = item.publishedAt,
                summary = item.summary ?: item.contentHtml,
                contentHtml = item.contentHtml,
            )
        }
        val trimmed = trimArticles(rows, policy())
        cached.values.removeAll { it.feedId == feedId }
        for (article in trimmed) {
            cached[article.id] = article
            if (library.readState(article.id) == null) {
                library.upsertReadState(ReadState(article.id, ModuleKind.News, ReadStatus.Unread, now))
            }
        }
        library.upsertFeed(
            feed.copy(title = parsed.title.ifBlank { feed.title }, siteUrl = parsed.siteUrl ?: feed.siteUrl, updatedAt = now),
        )
        index.save(feedId, trimmed)
        return trimmed
    }
}

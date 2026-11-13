package org.aetherfeed.app.data

import org.aetherfeed.app.data.local.ArticleBodyEntity
import org.aetherfeed.app.data.local.ArticleEntity
import org.aetherfeed.app.data.local.FeedEntity
import org.aetherfeed.app.data.local.PlaybackEntity
import org.aetherfeed.app.data.local.ReadStateEntity
import org.aetherfeed.app.data.local.StarEntity
import org.aetherfeed.app.data.local.TagEntity
import org.aetherfeed.app.data.local.VaultDatabase
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.PlaybackPosition
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.Star
import org.aetherfeed.app.domain.Tag
import org.aetherfeed.app.domain.totalUnread
import org.aetherfeed.app.sync.FeedChangeNotifier
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomLibraryRepository
@Inject
constructor(
    private val db: VaultDatabase,
    private val feedChangeNotifier: FeedChangeNotifier,
) : LibraryRepository {
    override suspend fun feeds(): List<Feed> = db.feeds().all().map { it.toDomain() }

    override suspend fun upsertFeed(feed: Feed) {
        db.feeds().upsert(feed.toEntity())
        // News, podcast, and booru join the Hermes subscription pool (Hermes hides booru in UI).
        if (feed.kind == ModuleKind.News || feed.kind == ModuleKind.Podcast || feed.kind == ModuleKind.Booru) {
            feedChangeNotifier.notifyChanged()
        }
    }

    override suspend fun deleteFeed(id: String) {
        db.feeds().delete(id)
        feedChangeNotifier.notifyChanged()
    }

    override suspend fun articles(feedId: String?): List<Article> =
        db.articles().byFeed(feedId).map { it.toDomain() }

    override suspend fun upsertArticle(article: Article) = db.articles().upsert(article.toEntity())

    override suspend fun articleBody(id: String): String? = db.articleBodies().get(id)

    override suspend fun cacheArticleBody(id: String, body: String) =
        db.articleBodies().upsert(ArticleBodyEntity(id, body))

    override suspend fun readState(targetId: String): ReadState? =
        db.readStates().get(targetId)?.toDomain()

    override suspend fun readStates(): List<ReadState> =
        db.readStates().all().map { it.toDomain() }

    override suspend fun upsertReadState(state: ReadState) =
        db.readStates().upsert(state.toEntity())

    override suspend fun stars(): List<Star> = db.stars().all().map { it.toDomain() }

    override suspend fun upsertStar(star: Star) = db.stars().upsert(star.toEntity())

    override suspend fun removeStar(targetId: String) = db.stars().delete(targetId)

    override suspend fun tags(): List<Tag> = db.tags().all().map { it.toDomain() }

    override suspend fun unreadCount(): Int = totalUnread(db.readStates().all().map { it.toDomain() })

    override suspend fun playback(episodeId: String): PlaybackPosition? =
        db.playback().get(episodeId)?.toDomain()

    override suspend fun playbackAll(): List<PlaybackPosition> =
        db.playback().all().map { it.toDomain() }

    override suspend fun upsertPlayback(position: PlaybackPosition) =
        db.playback().upsert(position.toEntity())
}

private fun FeedEntity.toDomain() = Feed(id, title, url, kind, siteUrl, updatedAt)

private fun Feed.toEntity() = FeedEntity(id, title, url, kind, siteUrl, updatedAt)

private fun ArticleEntity.toDomain() =
    Article(id, feedId, title, url, publishedAt, summary, localPath)

private fun Article.toEntity() =
    ArticleEntity(id, feedId, title, url, publishedAt, summary, localPath)

private fun ReadStateEntity.toDomain() = ReadState(targetId, module, status, updatedAt)

private fun ReadState.toEntity() = ReadStateEntity(targetId, module, status, updatedAt)

private fun StarEntity.toDomain() = Star(targetId, module, createdAt)

private fun Star.toEntity() = StarEntity(targetId, module, createdAt)

private fun TagEntity.toDomain() = Tag(id, name)

private fun Tag.toEntity() = TagEntity(id, name)

private fun PlaybackEntity.toDomain() = PlaybackPosition(episodeId, positionMs, durationMs, updatedAt)

private fun PlaybackPosition.toEntity() =
    PlaybackEntity(episodeId, positionMs, durationMs, updatedAt)

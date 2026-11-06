package org.aetherfeed.app.data

import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.PlaybackPosition
import org.aetherfeed.app.domain.Star
import java.io.File
import kotlinx.coroutines.runBlocking

/**
 * Durable local read path: cached bodies, stars, and playback under [root].
 * No network I/O — readable with the radio off.
 */
class OfflineReadPath(
    private val root: File,
    private val library: InMemoryLibrary = InMemoryLibrary(),
) {
    private val bodiesDir get() = File(root, "bodies").also { it.mkdirs() }
    private val metaFile get() = File(root, "meta.tsv")

    private val starRows = linkedMapOf<String, Star>()
    private val playbackRows = linkedMapOf<String, PlaybackPosition>()
    private val articleRows = linkedMapOf<String, Article>()

    init {
        root.mkdirs()
        reloadFromDisk()
    }

    fun library(): InMemoryLibrary = library

    fun radioOffBody(id: String): String? {
        val disk = File(bodiesDir, sanitize(id))
        return if (disk.isFile) disk.readText(Charsets.UTF_8) else null
    }

    suspend fun cacheForOffline(article: Article, body: String) {
        val path = File(bodiesDir, sanitize(article.id))
        path.writeText(body, Charsets.UTF_8)
        val cached = article.copy(localPath = path.path)
        articleRows[cached.id] = cached
        library.upsertArticle(cached)
        library.cacheArticleBody(cached.id, body)
        flushMeta()
    }

    suspend fun starLocal(targetId: String, module: ModuleKind, now: Long) {
        val star = Star(targetId, module, now)
        starRows[targetId] = star
        library.upsertStar(star)
        flushMeta()
    }

    suspend fun savePlayback(position: PlaybackPosition) {
        playbackRows[position.episodeId] = position
        library.upsertPlayback(position)
        flushMeta()
    }

    fun stars(): List<Star> = starRows.values.toList()

    fun playback(episodeId: String): PlaybackPosition? = playbackRows[episodeId]

    fun cachedArticles(): List<Article> = articleRows.values.toList()

    private fun reloadFromDisk() {
        if (!metaFile.isFile) return
        metaFile.readLines().forEach { line ->
            val p = line.split('\t')
            when (p.firstOrNull()) {
                "A" -> if (p.size >= 5) {
                    val article = Article(p[1], p[2], p[3], p[4], localPath = p.getOrNull(5))
                    articleRows[article.id] = article
                    runBlocking { library.upsertArticle(article) }
                }
                "B" -> if (p.size >= 2) {
                    val body = radioOffBody(p[1]) ?: return@forEach
                    runBlocking { library.cacheArticleBody(p[1], body) }
                }
                "S" -> if (p.size >= 4) {
                    val star = Star(p[1], ModuleKind.valueOf(p[2]), p[3].toLong())
                    starRows[star.targetId] = star
                    runBlocking { library.upsertStar(star) }
                }
                "P" -> if (p.size >= 4) {
                    val pos = PlaybackPosition(
                        episodeId = p[1],
                        positionMs = p[2].toLong(),
                        durationMs = p.getOrNull(3)?.ifBlank { null }?.toLongOrNull(),
                        updatedAt = p.getOrNull(4)?.toLongOrNull() ?: 0L,
                    )
                    playbackRows[pos.episodeId] = pos
                    runBlocking { library.upsertPlayback(pos) }
                }
            }
        }
    }

    private fun flushMeta() {
        val lines = mutableListOf<String>()
        articleRows.values.forEach { a ->
            lines += listOf("A", a.id, a.feedId, a.title, a.url, a.localPath.orEmpty())
                .joinToString("\t")
            lines += listOf("B", a.id).joinToString("\t")
        }
        starRows.values.forEach { s ->
            lines += listOf("S", s.targetId, s.module.name, s.createdAt.toString())
                .joinToString("\t")
        }
        playbackRows.values.forEach { p ->
            lines += listOf(
                "P",
                p.episodeId,
                p.positionMs.toString(),
                p.durationMs?.toString().orEmpty(),
                p.updatedAt.toString(),
            ).joinToString("\t")
        }
        metaFile.writeText(lines.joinToString("\n"), Charsets.UTF_8)
    }

    companion object {
        fun sanitize(id: String): String = id.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }
}

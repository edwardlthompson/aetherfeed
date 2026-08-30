package org.aetherfeed.app.boardmerge

import org.aetherfeed.app.domain.BooruPost

data class MergedPost(
    val post: BooruPost,
    val sources: Set<String>,
    val pool: String? = null,
    val note: String? = null,
)

fun mergePosts(rows: List<BooruPost>): List<MergedPost> {
    val buckets = linkedMapOf<String, MergedPost>()
    for (row in rows) {
        val key = row.remoteId.ifBlank { row.id }
        val existing = buckets[key]
        buckets[key] = if (existing == null) {
            MergedPost(row, setOf(row.sourceId))
        } else {
            existing.copy(sources = existing.sources + row.sourceId)
        }
    }
    return buckets.values.toList()
}

fun withNote(item: MergedPost, note: String): MergedPost = item.copy(note = note.trim().ifEmpty { null })

fun withPool(item: MergedPost, pool: String): MergedPost = item.copy(pool = pool.trim().ifEmpty { null })

package org.aetherfeed.app.boardauth

data class SourceKey(
    val sourceId: String,
    val apiKey: String,
    val userId: String? = null,
)

class SourceKeyStore {
    private val keys = mutableMapOf<String, SourceKey>()

    fun put(key: SourceKey) {
        require(key.sourceId.isNotBlank()) { "sourceId" }
        require(key.apiKey.isNotBlank()) { "apiKey" }
        keys[key.sourceId] = key
    }

    fun get(sourceId: String): SourceKey? = keys[sourceId]

    fun requiresKey(kind: String): Boolean =
        kind.equals("gelbooru", ignoreCase = true) || kind.equals("gelbooru-family", ignoreCase = true)
}

class MissingSourceKeyException(sourceId: String) : IllegalStateException("API key required for $sourceId")

fun gelbooruQuery(base: String, tags: List<String>, key: SourceKey): String {
    val joined = tags.joinToString("+") { it.trim() }.ifBlank { "all" }
    val user = key.userId?.let { "&user_id=$it" }.orEmpty()
    return "$base/index.php?page=dapi&s=post&q=index&json=1&tags=$joined&api_key=${key.apiKey}$user"
}

package org.aetherfeed.shared.vault

/**
 * SQLDelight target table names — must stay aligned with Room [VaultEntities]
 * (`feeds`, `articles`, `article_bodies`, `read_states`, `stars`, `tags`, `playback`).
 */
object VaultSchema {
    val TABLES =
        listOf(
            "feeds",
            "articles",
            "article_bodies",
            "read_states",
            "stars",
            "tags",
            "playback",
        )
}

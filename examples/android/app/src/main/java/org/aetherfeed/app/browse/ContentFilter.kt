package org.aetherfeed.app.browse

import java.net.URI

enum class SortOrder { Newest, Oldest }

data class TreeNode(
    val id: String,
    val label: String,
    val unreadCount: Int = 0,
    val children: List<TreeNode> = emptyList(),
)

object ContentFilter {
    fun <T> apply(
        items: List<T>,
        unreadOnly: Boolean,
        starredOnly: Boolean,
        inCategory: (T) -> Boolean,
        isUnread: (T) -> Boolean,
        isStarred: (T) -> Boolean,
    ): List<T> =
        items.filter { item ->
            inCategory(item) &&
                (!unreadOnly || isUnread(item)) &&
                (!starredOnly || isStarred(item))
        }

    fun <T> sortBy(
        items: List<T>,
        order: SortOrder,
        timestamp: (T) -> Long,
    ): List<T> {
        val sorted = items.sortedByDescending(timestamp)
        return if (order == SortOrder.Oldest) sorted.reversed() else sorted
    }

    fun folderKey(url: String): String =
        runCatching {
            URI(url).host?.removePrefix("www.")?.ifBlank { null }
        }.getOrNull() ?: "Other"

    fun feedTree(
        feeds: List<Pair<String, String>>,
        unreadByFeed: Map<String, Int> = emptyMap(),
        totalUnread: Int = 0,
        starredUnread: Int = 0,
        hideEmpty: Boolean = false,
        allLabel: String,
        starredLabel: String,
    ): List<TreeNode> {
        val folders = linkedMapOf<String, MutableList<TreeNode>>()
        for ((url, title) in feeds.sortedBy { it.second.ifBlank { it.first } }) {
            val folder = folderKey(url)
            folders.getOrPut(folder) { mutableListOf() }.add(
                TreeNode(url, title.ifBlank { url }, unreadByFeed[url] ?: 0),
            )
        }
        val nodes =
            mutableListOf(
                TreeNode("", allLabel, totalUnread),
                TreeNode("starred", starredLabel, starredUnread),
            )
        for ((folder, children) in folders.toSortedMap()) {
            val unread = children.sumOf { it.unreadCount }
            if (hideEmpty && unread == 0) continue
            val kept = if (hideEmpty) children.filter { it.unreadCount > 0 } else children
            if (kept.isEmpty()) continue
            nodes.add(TreeNode("folder:$folder", folder, unread, kept))
        }
        return nodes
    }

    fun matchesFolder(nodeId: String, feedUrl: String, feedId: String): Boolean {
        if (nodeId.isEmpty() || nodeId == "starred") return true
        if (nodeId == feedUrl || nodeId == feedId) return true
        if (nodeId.startsWith("folder:")) {
            val folder = nodeId.removePrefix("folder:")
            return folderKey(feedUrl) == folder || folderKey(feedId) == folder
        }
        return false
    }
}

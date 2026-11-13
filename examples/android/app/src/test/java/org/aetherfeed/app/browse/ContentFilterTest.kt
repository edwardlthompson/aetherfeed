package org.aetherfeed.app.browse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentFilterTest {
    data class Row(val id: String, val feed: String, val unread: Boolean, val starred: Boolean, val at: Long)

    @Test
    fun keepsUnreadAndSortsOldestFirst() {
        val rows =
            listOf(
                Row("a", "one", unread = true, starred = false, at = 2),
                Row("b", "two", unread = false, starred = true, at = 1),
            )
        val visible =
            ContentFilter.apply(
                rows,
                unreadOnly = true,
                starredOnly = false,
                inCategory = { it.feed == "one" },
                isUnread = { it.unread },
                isStarred = { it.starred },
            )
        assertEquals(listOf("a"), visible.map { it.id })
        assertEquals(
            listOf("b", "a"),
            ContentFilter.sortBy(rows, SortOrder.Oldest) { it.at }.map { it.id },
        )
    }

    @Test
    fun nestsFeedsUnderHostFolder() {
        val tree =
            ContentFilter.feedTree(
                feeds =
                    listOf(
                        "https://one.test/rss" to "One RSS",
                        "https://one.test/atom" to "One Atom",
                        "https://two.test/feed" to "Two",
                    ),
                unreadByFeed = mapOf("https://one.test/rss" to 2),
                totalUnread = 2,
                allLabel = "All",
                starredLabel = "Starred",
            )
        val one = tree.first { it.id == "folder:one.test" }
        assertEquals(listOf("One Atom", "One RSS"), one.children.map { it.label })
        assertTrue(ContentFilter.matchesFolder("folder:one.test", "https://one.test/rss", "https://one.test/rss"))
    }
}

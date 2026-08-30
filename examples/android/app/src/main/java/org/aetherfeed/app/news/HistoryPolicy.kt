package org.aetherfeed.app.news

import org.aetherfeed.app.domain.Article

const val HISTORY_COUNT_DEFAULT = 10
const val HISTORY_DAYS_MAX = 30
val HISTORY_COUNT_CHOICES = listOf(10, 25, 50, 100)
val HISTORY_DAYS_CHOICES = listOf(1, 7, 14, 30)

enum class HistoryMode { Count, Days }

data class HistoryPolicy(
    val mode: HistoryMode = HistoryMode.Count,
    val count: Int = HISTORY_COUNT_DEFAULT,
    val days: Int = 7,
)

fun trimArticles(
    rows: List<Article>,
    policy: HistoryPolicy,
    now: Long = System.currentTimeMillis(),
): List<Article> {
    val newestFirst = rows.sortedWith(
        compareByDescending<Article> { it.publishedAt ?: Long.MIN_VALUE }.thenBy { it.id },
    )
    return when (policy.mode) {
        HistoryMode.Count -> newestFirst.take(policy.count.coerceAtLeast(1))
        HistoryMode.Days -> {
            val window = policy.days.coerceIn(1, HISTORY_DAYS_MAX) * 86_400_000L
            val cutoff = now - window
            newestFirst.filter { row -> (row.publishedAt ?: now) >= cutoff }
        }
    }
}

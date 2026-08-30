package org.aetherfeed.app.ui.news

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import org.aetherfeed.app.domain.Article
import org.aetherfeed.app.news.neighborArticle
import org.aetherfeed.app.ui.ModeActionBus

@Composable
fun BindNewsActions(
    sorted: List<Article>,
    selected: Article?,
    starred: Set<String>,
    readIds: Set<String>,
    actions: ModeActionBus?,
    open: (Article) -> Unit,
    onStar: (Article) -> Unit,
    onToggleRead: (Article) -> Unit,
) {
    DisposableEffect(sorted, selected?.id, starred, readIds, actions) {
        if (actions != null) {
            val id = selected?.id
            actions.prev = neighborArticle(sorted, id, -1)?.let { row -> { open(row) } }
            actions.next = neighborArticle(sorted, id, 1)?.let { row -> { open(row) } }
            actions.star = selected?.let { row -> { onStar(row) } }
            actions.toggleRead = selected?.let { row -> { onToggleRead(row) } }
            actions.starred = id != null && id in starred
            actions.isRead = id != null && id in readIds
            actions.publish()
        }
        onDispose { actions?.clearNews() }
    }
}

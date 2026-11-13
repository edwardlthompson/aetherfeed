package org.aetherfeed.app.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.aetherfeed.app.R
import org.aetherfeed.app.browse.SortOrder
import org.aetherfeed.app.browse.TreeNode
import org.aetherfeed.app.ui.theme.SpacingSm

@Composable
fun ReaderNavPane(
    nodes: List<TreeNode>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .testTag("folder-tree"),
    ) {
        nodes.forEach { node ->
            TreeRows(node, selectedId, onSelect, depth = 0)
        }
    }
}

@Composable
private fun TreeRows(
    node: TreeNode,
    selectedId: String,
    onSelect: (String) -> Unit,
    depth: Int,
) {
    val label =
        buildString {
            append(if (node.id.isEmpty()) stringResource(R.string.filter_all) else node.label)
            if (node.unreadCount > 0) append("  ${node.unreadCount}")
        }
    Text(
        text = label,
        style =
            if (node.unreadCount > 0) {
                MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodyMedium
            },
        color =
            if (selectedId == node.id) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onSelect(node.id) }
                .padding(start = (12 * (depth + 1)).dp, top = SpacingSm, bottom = SpacingSm)
                .testTag("tree-node-${node.id.ifEmpty { "all" }}"),
    )
    node.children.forEach { child ->
        TreeRows(child, selectedId, onSelect, depth + 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortMenu(
    sort: SortOrder,
    onSort: (SortOrder) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value =
                stringResource(
                    if (sort == SortOrder.Newest) R.string.filter_sort_newest else R.string.filter_sort_oldest,
                ),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.filter_sort)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().width(180.dp).testTag("filter-sort"),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_sort_newest)) },
                onClick = {
                    onSort(SortOrder.Newest)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_sort_oldest)) },
                onClick = {
                    onSort(SortOrder.Oldest)
                    expanded = false
                },
            )
        }
    }
}

@Composable
fun ReaderSplit(
    nodes: List<TreeNode>,
    selectedId: String,
    onSelect: (String) -> Unit,
    unreadOnly: Boolean,
    onUnreadOnly: (Boolean) -> Unit,
    unreadLabel: String,
    sort: SortOrder,
    onSort: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth().testTag("browse-layout")) {
        ReaderNavPane(
            nodes = nodes,
            selectedId = selectedId,
            onSelect = onSelect,
            modifier = Modifier.width(200.dp).fillMaxHeight(),
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(SpacingSm)
                        .testTag("list-toolbar"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = unreadOnly,
                        onCheckedChange = onUnreadOnly,
                        modifier = Modifier.testTag("filter-unread"),
                    )
                    Text(unreadLabel, style = MaterialTheme.typography.bodyMedium)
                }
                SortMenu(sort, onSort)
            }
            content()
        }
    }
}

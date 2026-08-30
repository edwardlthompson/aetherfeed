package org.aetherfeed.app.ui.news

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import org.aetherfeed.app.R

@Composable
internal fun NewsFilterMenu(oldestFirst: Boolean, onOldestFirst: (Boolean) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.testTag("news-sort")) {
            Icon(Icons.Outlined.FilterList, contentDescription = stringResource(R.string.news_filter))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.news_sort_newest)) },
                onClick = { onOldestFirst(false); open = false },
                enabled = oldestFirst,
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.news_sort_oldest)) },
                onClick = { onOldestFirst(true); open = false },
                enabled = !oldestFirst,
            )
        }
    }
}

package org.aetherfeed.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.aetherfeed.app.R
import org.aetherfeed.app.data.RoomLibraryRepository
import org.aetherfeed.app.data.SqlCipherVault
import org.aetherfeed.app.readerimport.ReaderImportRepository
import org.aetherfeed.app.readerimport.parseReaderImport
import org.aetherfeed.app.readerimport.readerImportSummary
import org.aetherfeed.app.readerimport.uriToImportFile

@Composable
fun ImportSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vault = remember {
        runCatching { RoomLibraryRepository(SqlCipherVault().open(context).libraryDao()) }.getOrNull()
    }
    var resultText by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null || vault == null) return@rememberLauncherForActivityResult
        scope.launch {
            val file = runCatching { uriToImportFile(context, uri) }.getOrElse {
                resultText = context.getString(R.string.readerimport_read_failed)
                return@launch
            }
            resultText = readerImportSummary(ReaderImportRepository(vault).apply(parseReaderImport(file)))
        }
    }
    Column(modifier) {
        TextButton(
            onClick = { picker.launch(arrayOf("text/*", "application/xml", "application/json", "application/zip", "*/*")) },
            modifier = Modifier.testTag("readerimport-open"),
        ) {
            Text(stringResource(R.string.readerimport_open))
        }
        if (resultText.isNotBlank()) {
            Text(resultText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("readerimport-result"))
        }
    }
}

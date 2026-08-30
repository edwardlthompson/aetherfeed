package org.aetherfeed.app.readerimport

import android.content.Context
import android.net.Uri
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

fun isZipBytes(bytes: ByteArray): Boolean =
    bytes.size >= 2 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()

fun unzipTextMembers(bytes: ByteArray): Map<String, String> {
    val members = linkedMapOf<String, String>()
    ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
        var entry = zip.nextEntry
        while (entry != null) {
            val name = entry.name
            if (!entry.isDirectory && Regex("\\.(xml|opml|json)$", RegexOption.IGNORE_CASE).containsMatchIn(name)) {
                members[name] = zip.readBytes().toString(Charsets.UTF_8)
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
    }
    return members
}

fun bytesToImportFile(name: String, bytes: ByteArray): ReaderImportFile {
    if (bytes.isEmpty()) return ReaderImportFile(name, text = "")
    if (isZipBytes(bytes) || name.endsWith(".zip", ignoreCase = true)) {
        return ReaderImportFile(name, members = unzipTextMembers(bytes))
    }
    return ReaderImportFile(name, text = bytes.toString(Charsets.UTF_8))
}

fun uriToImportFile(context: Context, uri: Uri): ReaderImportFile {
    val name = uri.lastPathSegment ?: "import.opml"
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    return bytesToImportFile(name, bytes)
}

package org.aetherfeed.app.ui.navigation

private const val SEP = '\u001f'

sealed class LibraryPick {
    data object Unified : LibraryPick()
    data class All(val mode: AppDestination) : LibraryPick()
    data class Folder(val mode: AppDestination, val folder: String) : LibraryPick()
    data class Source(val mode: AppDestination, val folder: String, val sourceId: String) : LibraryPick()
}

fun LibraryPick.destination(): AppDestination = when (this) {
    LibraryPick.Unified -> AppDestination.News
    is LibraryPick.All -> mode
    is LibraryPick.Folder -> mode
    is LibraryPick.Source -> mode
}

fun LibraryPick.parent(): LibraryPick? = when (this) {
    LibraryPick.Unified -> null
    is LibraryPick.All -> null
    is LibraryPick.Folder -> LibraryPick.All(mode)
    is LibraryPick.Source -> LibraryPick.Folder(mode, folder)
}

fun LibraryPick.expandKeys(): Set<String> = when (this) {
    LibraryPick.Unified -> emptySet()
    is LibraryPick.All -> setOf(mode.name)
    is LibraryPick.Folder -> setOf(mode.name, "${mode.name}:$folder")
    is LibraryPick.Source -> setOf(mode.name, "${mode.name}:$folder")
}

fun encodeLibraryPick(pick: LibraryPick): String = when (pick) {
    LibraryPick.Unified -> "unified"
    is LibraryPick.All -> "all$SEP${pick.mode.name}"
    is LibraryPick.Folder -> "folder$SEP${pick.mode.name}$SEP${pick.folder}"
    is LibraryPick.Source -> "source$SEP${pick.mode.name}$SEP${pick.folder}$SEP${pick.sourceId}"
}

fun parseLibraryPick(raw: String?): LibraryPick {
    val text = raw?.trim().orEmpty()
    if (text.isEmpty() || text == "unified") return LibraryPick.Unified
    val parts = text.split(SEP)
    return when (parts.firstOrNull()) {
        "all" -> LibraryPick.All(parseAppDestination(parts.getOrNull(1)))
        "folder" -> {
            val mode = parseAppDestination(parts.getOrNull(1))
            val folder = parts.getOrNull(2)?.trim().orEmpty()
            if (folder.isEmpty()) LibraryPick.All(mode) else LibraryPick.Folder(mode, folder)
        }
        "source" -> {
            val mode = parseAppDestination(parts.getOrNull(1))
            val folder = parts.getOrNull(2)?.trim().orEmpty()
            val id = parts.drop(3).joinToString(SEP.toString()).trim()
            if (id.isEmpty()) LibraryPick.All(mode) else LibraryPick.Source(mode, folder, id)
        }
        else -> LibraryPick.All(AppDestination.News)
    }
}

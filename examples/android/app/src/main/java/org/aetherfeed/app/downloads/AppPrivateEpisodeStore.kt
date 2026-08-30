package org.aetherfeed.app.downloads

import java.io.File

/** Writes episode bytes under an app-private files directory. Never public storage. */
class AppPrivateEpisodeStore(private val filesDir: File) {
    fun episodesDir(): File = File(filesDir, EPISODES_DIR).also { it.mkdirs() }

    fun fileFor(episodeId: String): File = File(episodesDir(), "${sanitizeEpisodeId(episodeId)}.bin")

    fun write(episodeId: String, bytes: ByteArray): File {
        require(bytes.isNotEmpty()) { "empty bytes" }
        val key = org.aetherfeed.app.applock.AppLockHolder.vaultKey()
        if (key != null) {
            return org.aetherfeed.app.applock.EncryptedCache(filesDir) { key }.write("episodes", episodeId, bytes)
        }
        val dest = fileFor(episodeId)
        dest.parentFile?.mkdirs()
        dest.writeBytes(bytes)
        check(isUnderFilesDir(dest)) { "refused path outside app-private files" }
        return dest
    }

    fun delete(episodeId: String): Boolean {
        val dest = fileFor(episodeId)
        return !dest.exists() || dest.delete()
    }

    fun isUnderFilesDir(path: File): Boolean {
        val root = filesDir.canonicalFile
        val target = path.canonicalFile
        return target == root || target.path.startsWith(root.path + File.separator)
    }
}

internal fun sanitizeEpisodeId(id: String): String {
    val trimmed = id.trim()
    require(trimmed.isNotEmpty()) { "episodeId" }
    return trimmed.replace(UNSAFE_ID, "_")
}

private const val EPISODES_DIR = "episodes"
private val UNSAFE_ID = Regex("[^A-Za-z0-9._-]")

package org.aetherfeed.app.applock

import android.util.Log
import java.io.File

class EncryptedCache(
    private val filesDir: File,
    private val keyProvider: () -> ByteArray?,
) {
    fun write(kind: String, id: String, bytes: ByteArray): File {
        val key = keyProvider() ?: error("locked")
        require(bytes.isNotEmpty()) { "empty bytes" }
        val dir = File(filesDir, kind).also { it.mkdirs() }
        val dest = File(dir, sanitize(id) + ".enc")
        dest.writeBytes(wrapCache(bytes, key))
        SessionPlainCache.put(kind, id, bytes)
        return dest
    }

    fun read(kind: String, id: String): ByteArray? {
        SessionPlainCache.get(kind, id)?.let { return it }
        val key = keyProvider() ?: return null
        val dest = destFile(kind, id)
        if (!dest.isFile) return null
        val blob = dest.readBytes()
        val started = System.nanoTime()
        val plain = runCatching { openCache(blob, key) }.getOrNull() ?: return null
        val ms = (System.nanoTime() - started) / 1_000_000
        val version = cacheVersion(blob)
        Log.i("AetherFeed", "cache.read $kind/${sanitize(id)} ${ms}ms v=$version n=${blob.size}")
        if (version < 3) {
            runCatching { dest.writeBytes(wrapCache(plain, key)) }
        }
        SessionPlainCache.put(kind, id, plain)
        return plain
    }

    fun exists(kind: String, id: String): Boolean {
        val dest = destFile(kind, id)
        return dest.isFile && dest.length() > 0
    }

    fun delete(kind: String, id: String) {
        destFile(kind, id).delete()
        SessionPlainCache.remove(kind, id)
    }

    fun ids(kind: String): List<String> {
        val dir = File(filesDir, kind)
        if (!dir.isDirectory) return emptyList()
        return dir.listFiles()
            ?.mapNotNull { file -> file.name.removeSuffix(".enc").takeIf { file.isFile && it.isNotEmpty() } }
            .orEmpty()
    }

    private fun destFile(kind: String, id: String): File =
        File(File(filesDir, kind), sanitize(id) + ".enc")

    private fun sanitize(id: String): String = id.trim().replace(UNSAFE, "_")
}

private val UNSAFE = Regex("[^A-Za-z0-9._-]")

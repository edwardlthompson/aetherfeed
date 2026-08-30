package org.aetherfeed.app.applock

object SessionPlainCache {
    private val lock = Any()
    private val bytes = LinkedHashMap<String, ByteArray>()

    fun get(kind: String, id: String): ByteArray? = synchronized(lock) { bytes[slot(kind, id)] }

    fun utf8(kind: String, id: String): String? = get(kind, id)?.toString(Charsets.UTF_8)

    fun put(kind: String, id: String, value: ByteArray) {
        if (value.isEmpty()) return
        synchronized(lock) { bytes[slot(kind, id)] = value }
    }

    fun ids(kind: String): Set<String> = synchronized(lock) {
        val prefix = "$kind\u0000"
        bytes.keys.mapNotNull { key -> key.takeIf { it.startsWith(prefix) }?.substring(prefix.length) }.toSet()
    }

    fun remove(kind: String, id: String) {
        synchronized(lock) { bytes.remove(slot(kind, id)) }
    }

    fun clear() {
        synchronized(lock) { bytes.clear() }
    }

    private fun slot(kind: String, id: String): String = "$kind\u0000$id"
}

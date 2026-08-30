package org.aetherfeed.app.about

object ProductUpdate {
    const val MS_DAY = 86_400_000L

    data class NamedAsset(val name: String, val url: String)
    data class ProductAsset(val version: String, val url: String)

    fun shouldCheckDaily(lastCheckAt: Long?, now: Long): Boolean {
        if (lastCheckAt == null || lastCheckAt < 0L) return true
        return now - lastCheckAt >= MS_DAY
    }

    fun isNewerVersion(current: String, latest: String): Boolean {
        fun parts(v: String) = v.split('.').map { it.toIntOrNull() ?: 0 }
        val a = parts(current)
        val b = parts(latest)
        for (i in 0..2) {
            val diff = (a.getOrElse(i) { 0 }) - (b.getOrElse(i) { 0 })
            if (diff != 0) return diff < 0
        }
        return false
    }

    fun parseApkVersion(name: String): String? {
        val match = Regex("aetherfeed-(\\d+\\.\\d+\\.\\d+)-foss\\.apk", RegexOption.IGNORE_CASE)
            .find(name.trim())
        return match?.groupValues?.get(1)
    }

    fun parseExeVersion(name: String): String? {
        val match = Regex("AetherFeed-(\\d+\\.\\d+\\.\\d+)", RegexOption.IGNORE_CASE)
            .find(name.trim())
        return match?.groupValues?.get(1)
    }

    fun selectApkAsset(assets: List<NamedAsset>): ProductAsset? {
        for (asset in assets) {
            val version = parseApkVersion(asset.name) ?: continue
            if (asset.url.isNotBlank()) return ProductAsset(version, asset.url)
        }
        return null
    }

    fun shouldNudgeDonate(lastSeenVersion: String?, currentVersion: String): Boolean {
        if (currentVersion.isBlank()) return false
        if (lastSeenVersion.isNullOrBlank()) return false
        return lastSeenVersion.trim() != currentVersion.trim()
    }

    fun shouldPromptUpdate(
        currentVersion: String,
        latestVersion: String?,
        dismissedVersion: String?,
    ): Boolean {
        if (latestVersion.isNullOrBlank()) return false
        if (!isNewerVersion(currentVersion, latestVersion)) return false
        if (dismissedVersion == latestVersion) return false
        return true
    }
}

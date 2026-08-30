package org.aetherfeed.app.about

object AppLaunchPrompts {
    sealed class Prompt {
        data object Donate : Prompt()
        data class Update(val version: String, val url: String) : Prompt()
    }

    fun decide(
        lastSeenVersion: String?,
        currentVersion: String,
        lastCheckAt: Long?,
        dismissedVersion: String?,
        now: Long,
        checksEnabled: Boolean,
        release: GithubRelease.Parsed?,
    ): DecideResult {
        if (ProductUpdate.shouldNudgeDonate(lastSeenVersion, currentVersion)) {
            return DecideResult(Prompt.Donate, seen = false, checked = false)
        }
        if (!checksEnabled || !ProductUpdate.shouldCheckDaily(lastCheckAt, now)) {
            return DecideResult(null, seen = true, checked = false)
        }
        val asset = release?.let { ProductUpdate.selectApkAsset(it.assets) }
        val latest = asset?.version
        if (!ProductUpdate.shouldPromptUpdate(currentVersion, latest, dismissedVersion) || latest == null) {
            return DecideResult(null, seen = true, checked = true)
        }
        val url = asset.url.ifBlank { null } ?: release.htmlUrl.ifBlank { DonateLinks.RELEASES_PAGE }
        return DecideResult(Prompt.Update(latest, url), seen = true, checked = true)
    }

    data class DecideResult(
        val prompt: Prompt?,
        val seen: Boolean,
        val checked: Boolean,
    )
}

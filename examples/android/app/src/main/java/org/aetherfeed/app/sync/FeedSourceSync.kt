package org.aetherfeed.app.sync

import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.readerimport.normalizeFeedUrl

data class FeedSyncResult(val count: Int, val pulled: Int)

suspend fun validAccessToken(prefs: DrivePrefs): String {
    val tokens = prefs.tokens() ?: error("Google Drive is not connected")
    if (tokens.expiresAt - 60_000 > System.currentTimeMillis()) return tokens.accessToken
    val refresh = tokens.refreshToken ?: error("Drive session expired — connect again")
    val next = refreshDriveToken(prefs.clientId(), prefs.clientSecret(), refresh)
    prefs.saveTokens(next)
    return next.accessToken
}

suspend fun syncFeedSources(prefs: DrivePrefs, library: LibraryRepository, now: Long = System.currentTimeMillis()): FeedSyncResult {
    val passphrase = prefs.passphrase()
    require(passphrase.isNotBlank()) { "Set the same sync passphrase on phone and PC" }
    val token = validAccessToken(prefs)
    val remoteBlob = pullFeedsBlob(token)
    val remote = if (remoteBlob == null) emptyList() else decodeFeedDocument(openJson(remoteBlob, passphrase))
    val existing = library.feeds()
    val merged = mergeFeedSources(existing, remote)
    val keep = merged.map { normalizeFeedUrl(it.url) }.toSet()
    for (old in existing) {
        if (normalizeFeedUrl(old.url) in keep && merged.none { it.id == old.id }) {
            library.deleteFeed(old.id)
        }
    }
    for (feed in merged) library.upsertFeed(feed)
    pushFeedsBlob(token, sealJson(encodeFeedDocument(merged, now), passphrase))
    return FeedSyncResult(count = merged.size, pulled = remote.size)
}

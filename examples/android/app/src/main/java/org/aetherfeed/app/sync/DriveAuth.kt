package org.aetherfeed.app.sync

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

fun loopbackRedirect(): String = "http://127.0.0.1:$DRIVE_LOOPBACK_PORT"

fun createPkceVerifier(): String = b64url(randomBytes(32))

fun createPkceChallenge(verifier: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
    return b64url(digest)
}

fun buildAuthUrl(clientId: String, verifier: String): String {
    val challenge = createPkceChallenge(verifier)
    val params = listOf(
        "client_id" to clientId,
        "redirect_uri" to loopbackRedirect(),
        "response_type" to "code",
        "scope" to DRIVE_APPDATA_SCOPE,
        "code_challenge" to challenge,
        "code_challenge_method" to "S256",
        "access_type" to "offline",
        "prompt" to "consent",
    ).joinToString("&") { (k, v) ->
        "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}"
    }
    return "https://accounts.google.com/o/oauth2/v2/auth?$params"
}

fun exchangeAuthCode(clientId: String, clientSecret: String, code: String, verifier: String): DriveTokens {
    val fields = mutableMapOf(
        "client_id" to clientId,
        "code" to code,
        "code_verifier" to verifier,
        "grant_type" to "authorization_code",
        "redirect_uri" to loopbackRedirect(),
    )
    if (clientSecret.isNotBlank()) fields["client_secret"] = clientSecret
    return parseTokens(formPost("https://oauth2.googleapis.com/token", fields))
}

fun refreshDriveToken(clientId: String, clientSecret: String, refreshToken: String): DriveTokens {
    val fields = mutableMapOf(
        "client_id" to clientId,
        "grant_type" to "refresh_token",
        "refresh_token" to refreshToken,
    )
    if (clientSecret.isNotBlank()) fields["client_secret"] = clientSecret
    val next = parseTokens(formPost("https://oauth2.googleapis.com/token", fields))
    return next.copy(refreshToken = next.refreshToken ?: refreshToken)
}

private fun parseTokens(obj: org.json.JSONObject): DriveTokens {
    val access = obj.optString("access_token")
    require(access.isNotBlank()) { "Drive token missing" }
    return DriveTokens(
        accessToken = access,
        refreshToken = obj.optString("refresh_token").ifBlank { null },
        expiresAt = System.currentTimeMillis() + obj.optLong("expires_in", 3600) * 1000,
    )
}

private fun randomBytes(size: Int): ByteArray = ByteArray(size).also { SecureRandom().nextBytes(it) }

private fun b64url(bytes: ByteArray): String =
    Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

package org.aetherfeed.app.sync

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

data class LanMeta(
    val etag: String,
    val authSaltB64: String,
)

sealed class LanHttpResult {
    data class Ok(val body: String, val etag: String, val authSaltB64: String = "") : LanHttpResult()

    data class Empty(val etag: String, val authSaltB64: String = "") : LanHttpResult()

    data class Failed(val code: Int, val message: String) : LanHttpResult()
}

/** Thin HTTP client for desktop LAN hub (5s timeouts). */
object LanPoolHttp {
    private const val TIMEOUT_MS = 5_000
    const val HEADER_AUTH_SALT = "X-Auth-Salt-B64"

    fun getMeta(hubUrl: String): LanMeta {
        val conn = open("$hubUrl/v1/meta", "GET")
        return try {
            val code = conn.responseCode
            val body = readBody(conn)
            if (code !in 200..299) {
                throw IllegalStateException("meta HTTP $code")
            }
            val obj = JSONObject(body)
            LanMeta(
                etag = obj.optString("etag", "0"),
                authSaltB64 = obj.getString("authSaltB64"),
            )
        } finally {
            conn.disconnect()
        }
    }

    fun getPool(hubUrl: String): LanHttpResult {
        val conn = open("$hubUrl/v1/pool", "GET")
        return try {
            val code = conn.responseCode
            val etag = conn.getHeaderField("etag") ?: "0"
            val salt = conn.getHeaderField(HEADER_AUTH_SALT).orEmpty()
            when (code) {
                404 -> LanHttpResult.Empty(etag, salt)
                in 200..299 -> LanHttpResult.Ok(readBody(conn), etag, salt)
                else -> LanHttpResult.Failed(code, readBody(conn).ifBlank { "get failed" })
            }
        } finally {
            conn.disconnect()
        }
    }

    fun putPool(
        hubUrl: String,
        envelopeJson: String,
        etag: String,
        bearer: String,
    ): LanHttpResult {
        val conn = open("$hubUrl/v1/pool", "PUT")
        return try {
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("If-Match", etag)
            conn.setRequestProperty("Authorization", "Bearer $bearer")
            conn.doOutput = true
            conn.outputStream.use { it.write(envelopeJson.toByteArray(StandardCharsets.UTF_8)) }
            val code = conn.responseCode
            val next = conn.getHeaderField("etag") ?: etag
            when (code) {
                in 200..299 -> LanHttpResult.Ok("", next)
                412 -> LanHttpResult.Failed(412, "precondition failed")
                else -> LanHttpResult.Failed(code, readBody(conn).ifBlank { "put failed" })
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun open(url: String, method: String): HttpURLConnection {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = TIMEOUT_MS
        conn.readTimeout = TIMEOUT_MS
        conn.requestMethod = method
        conn.instanceFollowRedirects = false
        return conn
    }

    private fun readBody(conn: HttpURLConnection): String {
        val stream =
            try {
                conn.inputStream
            } catch (_: Exception) {
                conn.errorStream
            } ?: return ""
        return stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
    }
}

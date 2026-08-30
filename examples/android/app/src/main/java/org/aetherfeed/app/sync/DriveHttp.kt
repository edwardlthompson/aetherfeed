package org.aetherfeed.app.sync

import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import org.json.JSONObject

fun httpJson(method: String, url: String, token: String? = null, body: String? = null, contentType: String? = null): JSONObject {
    val text = httpText(method, url, token, body?.toByteArray(Charsets.UTF_8), contentType)
    return if (text.isBlank()) JSONObject() else JSONObject(text)
}

fun httpBytes(method: String, url: String, token: String, body: ByteArray? = null, contentType: String? = null): ByteArray {
    val conn = open(method, url, token, contentType)
    if (body != null) conn.outputStream.use { it.write(body) }
    return readBytes(conn)
}

fun httpText(method: String, url: String, token: String? = null, body: ByteArray? = null, contentType: String? = null): String {
    val conn = open(method, url, token, contentType)
    if (body != null) conn.outputStream.use { it.write(body) }
    return readBytes(conn).toString(Charsets.UTF_8)
}

fun formPost(url: String, fields: Map<String, String>): JSONObject {
    val encoded = fields.entries.joinToString("&") { (k, v) ->
        "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}"
    }
    return httpJson("POST", url, body = encoded, contentType = "application/x-www-form-urlencoded")
}

private fun open(method: String, url: String, token: String?, contentType: String?): HttpURLConnection {
    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
        try {
            requestMethod = method
        } catch (_: ProtocolException) {
            requestMethod = "POST"
            setRequestProperty("X-HTTP-Method-Override", method)
        }
        connectTimeout = 20_000
        readTimeout = 20_000
        doInput = true
        if (bodyExpected(method)) doOutput = true
        if (token != null) setRequestProperty("Authorization", "Bearer $token")
        if (contentType != null) setRequestProperty("Content-Type", contentType)
    }
    return conn
}

private fun bodyExpected(method: String): Boolean = method == "POST" || method == "PATCH" || method == "PUT"

private fun readBytes(conn: HttpURLConnection): ByteArray {
    val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
    val bytes = stream?.readBytes() ?: ByteArray(0)
    if (conn.responseCode !in 200..299) {
        throw IllegalStateException("HTTP ${conn.responseCode}: ${bytes.toString(Charsets.UTF_8)}")
    }
    return bytes
}

package org.aetherfeed.app.news

import java.net.HttpURLConnection
import java.net.URL

object ArticleFetch {
    fun html(url: String): String? {
        if (!ArticleImages.isHttpUrl(url)) return null
        val conn =
            (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 20_000
                instanceFollowRedirects = true
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/131.0.0.0 Mobile Safari/537.36",
                )
            }
        return try {
            if (conn.responseCode !in 200..299) {
                null
            } else {
                conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            }
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }
}

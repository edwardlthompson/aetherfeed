package org.aetherfeed.app.about

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GithubRelease {
    data class Parsed(val htmlUrl: String, val assets: List<ProductUpdate.NamedAsset>)

    fun parse(json: String): Parsed? {
        return try {
            val root = JSONObject(json)
            val htmlUrl = root.optString("html_url").ifBlank { DonateLinks.RELEASES_PAGE }
            val assets = mutableListOf<ProductUpdate.NamedAsset>()
            val arr = root.optJSONArray("assets")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    val name = item.optString("name", "")
                    val url = item.optString("browser_download_url", "")
                    if (name.isNotEmpty() && url.isNotEmpty()) {
                        assets.add(ProductUpdate.NamedAsset(name, url))
                    }
                }
            }
            Parsed(htmlUrl, assets)
        } catch (_: Exception) {
            null
        }
    }

    fun fetchLatest(userAgent: String): Parsed? {
        val conn = URL(DonateLinks.RELEASES_API).openConnection() as HttpURLConnection
        return try {
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", userAgent)
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return null
            parse(conn.inputStream.bufferedReader().use { it.readText() })
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }
}

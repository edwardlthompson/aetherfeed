package org.aetherfeed.app.sync

import java.net.URLEncoder

private const val FILES = "https://www.googleapis.com/drive/v3/files"
private const val UPLOAD = "https://www.googleapis.com/upload/drive/v3/files"

fun findFeedsFileId(token: String): String? {
    val q = URLEncoder.encode("name = '$DRIVE_FEEDS_NAME'", "UTF-8")
    val json = httpJson("GET", "$FILES?spaces=appDataFolder&fields=files(id,name)&q=$q", token)
    val files = json.optJSONArray("files") ?: return null
    return if (files.length() == 0) null else files.getJSONObject(0).optString("id").ifBlank { null }
}

fun pullFeedsBlob(token: String): ByteArray? {
    val id = findFeedsFileId(token) ?: return null
    return httpBytes("GET", "$FILES/$id?alt=media", token)
}

fun pushFeedsBlob(token: String, blob: ByteArray) {
    require(blob.isNotEmpty()) { "empty sync blob" }
    val existing = findFeedsFileId(token)
    if (existing != null) {
        httpBytes("PATCH", "$UPLOAD/$existing?uploadType=media", token, blob, "application/octet-stream")
        return
    }
    val boundary = "aetherfeed"
    val meta = """{"name":"$DRIVE_FEEDS_NAME","parents":["appDataFolder"]}"""
    val head = "--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n$meta\r\n--$boundary\r\nContent-Type: application/octet-stream\r\n\r\n"
    val tail = "\r\n--$boundary--"
    val body = head.toByteArray() + blob + tail.toByteArray()
    httpBytes("POST", "$UPLOAD?uploadType=multipart", token, body, "multipart/related; boundary=$boundary")
}

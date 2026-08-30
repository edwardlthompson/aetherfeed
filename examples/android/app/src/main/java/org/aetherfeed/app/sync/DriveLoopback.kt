package org.aetherfeed.app.sync

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import java.net.URLDecoder

fun waitForOauthCode(port: Int = DRIVE_LOOPBACK_PORT): String {
    ServerSocket(port, 1, InetAddress.getByName("127.0.0.1")).use { server ->
        server.soTimeout = 180_000
        val socket = server.accept()
        socket.use { client ->
            val request = BufferedReader(InputStreamReader(client.getInputStream(), Charsets.US_ASCII)).readLine()
                ?: throw IllegalStateException("empty OAuth redirect")
            val path = request.substringAfter(' ').substringBefore(' ')
            val query = path.substringAfter('?', "")
            val params = query.split('&').mapNotNull { pair ->
                val key = pair.substringBefore('=')
                val value = pair.substringAfter('=', "")
                if (key.isBlank()) null else key to URLDecoder.decode(value, "UTF-8")
            }.toMap()
            val html = "<html><body>AetherFeed can close this window.</body></html>"
            client.getOutputStream().write(
                "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: ${html.length}\r\n\r\n$html"
                    .toByteArray(Charsets.US_ASCII),
            )
            params["error"]?.let { throw IllegalStateException("Drive auth $it") }
            return params["code"] ?: throw IllegalStateException("missing OAuth code")
        }
    }
}

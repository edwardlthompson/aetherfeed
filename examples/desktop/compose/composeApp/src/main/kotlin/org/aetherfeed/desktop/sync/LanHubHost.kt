package org.aetherfeed.desktop.sync

/**
 * Home-LAN encrypted pool **host** on desktop.
 *
 * Behavior reference: `examples/desktop/src-tauri/src/lan_hub.rs`
 * — default port [DEFAULT_PORT], `GET/PUT /v1/pool`, private-LAN URL list.
 */
object LanHubDefaults {
    const val DEFAULT_PORT: Int = 8787
}

data class LanHubStatus(
    val running: Boolean,
    val port: Int,
    val etag: String,
    val lanUrls: List<String>,
)

interface LanHubHost {
    fun status(): LanHubStatus

    fun configure(passphrase: String, port: Int = LanHubDefaults.DEFAULT_PORT)

    fun start(): Result<Unit>

    fun stop()
}

/** Stub host for Sprint 59 row 1 — no HTTP listener yet. */
class StubLanHubHost : LanHubHost {
    private var passphrase: String = ""
    private var port: Int = LanHubDefaults.DEFAULT_PORT
    private var running: Boolean = false

    override fun status(): LanHubStatus =
        LanHubStatus(
            running = running,
            port = port,
            etag = "0",
            lanUrls = if (running) listOf("http://127.0.0.1:$port") else emptyList(),
        )

    override fun configure(passphrase: String, port: Int) {
        this.passphrase = passphrase
        this.port = port
    }

    override fun start(): Result<Unit> {
        if (passphrase.isBlank()) {
            return Result.failure(IllegalStateException("LAN passphrase required"))
        }
        running = true
        return Result.success(Unit)
    }

    override fun stop() {
        running = false
    }
}
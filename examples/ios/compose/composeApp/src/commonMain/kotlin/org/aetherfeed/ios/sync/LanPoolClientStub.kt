package org.aetherfeed.ios.sync

import org.aetherfeed.shared.domain.sync.LanPoolSettings

/** LAN client stub — full Ktor client lands with vault-backed CMP shell (Hermes N/A on iOS). */
class LanPoolClientStub {
  private var settings: LanPoolSettings = LanPoolSettings()
  var lastStatus: String = "LAN pool idle"
    private set

  fun configure(settings: LanPoolSettings) {
    this.settings = settings
    lastStatus = if (settings.ready()) "LAN settings saved." else "Set hub URL and passphrase."
  }

  fun syncNow(): String {
    if (!settings.ready()) {
      lastStatus = "Set hub URL and passphrase"
      return lastStatus
    }
    lastStatus = "LAN sync queued (stub client)."
    return lastStatus
  }
}
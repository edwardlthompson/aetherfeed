package org.aetherfeed.shared.domain.sync

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LanPoolSettingsTest {
    @Test
    fun readyWhenHubAndPassphraseSet() {
        assertFalse(LanPoolSettings().ready())
        assertTrue(LanPoolSettings(hubUrl = "http://127.0.0.1:8787", passphrase = "x").ready())
    }
}
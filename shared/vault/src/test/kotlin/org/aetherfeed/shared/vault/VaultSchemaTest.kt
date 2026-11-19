package org.aetherfeed.shared.vault

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VaultSchemaTest {
    @Test
    fun tablesMatchRoomInventory() {
        assertEquals(
            listOf(
                "feeds",
                "articles",
                "article_bodies",
                "read_states",
                "stars",
                "tags",
                "playback",
            ),
            VaultSchema.TABLES,
        )
    }

    @Test
    fun inMemoryKeychainRoundTrip() {
        val kc = InMemoryVaultKeychain()
        kc.storeSecret("lan", byteArrayOf(1, 2, 3))
        assertTrue(kc.loadSecret("lan").contentEquals(byteArrayOf(1, 2, 3)))
        kc.deleteSecret("lan")
        assertEquals(null, kc.loadSecret("lan"))
    }
}

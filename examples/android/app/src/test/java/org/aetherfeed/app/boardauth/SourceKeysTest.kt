package org.aetherfeed.app.boardauth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceKeysTest {
    @Test
    fun gelbooruRequiresKey() {
        val store = SourceKeyStore()
        assertTrue(store.requiresKey("gelbooru"))
        assertFalse(store.requiresKey("danbooru"))
    }

    @Test
    fun queryIncludesApiKey() {
        val url = gelbooruQuery(
            "https://example.invalid",
            listOf("sky"),
            SourceKey("gb", "secret", "9"),
        )
        assertTrue(url.contains("api_key=secret"))
        assertTrue(url.contains("user_id=9"))
    }
}

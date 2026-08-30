package org.aetherfeed.app.boardsafety

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeModeTest {
    @Test
    fun defaultHidesExplicit() {
        val settings = SafeModeSettings()
        assertTrue(allowPost(listOf("sky"), "s", settings))
        assertFalse(allowPost(listOf("sky"), "e", settings))
        assertFalse(allowPost(listOf("rating:e"), null, settings))
    }

    @Test
    fun blacklistBlocksTag() {
        val settings = SafeModeSettings(blacklist = setOf("spam"))
        assertFalse(allowPost(listOf("SPAM"), "s", settings))
    }
}

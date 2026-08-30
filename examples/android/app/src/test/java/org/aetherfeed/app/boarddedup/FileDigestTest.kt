package org.aetherfeed.app.boarddedup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FileDigestTest {
    @Test
    fun hashesBytes() {
        assertEquals(fileDigest("dup".toByteArray()), fileDigest("dup".toByteArray()))
        assertNotEquals(fileDigest("a".toByteArray()), fileDigest("b".toByteArray()))
    }
}

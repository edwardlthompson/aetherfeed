package org.aetherfeed.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLockTest {
    @Test
    fun classifyPinAndPassphrase() {
        assertEquals(AppLockSecretKind.Pin, classifyLockSecret("123456"))
        assertNull(classifyLockSecret("12345"))
        assertEquals(AppLockSecretKind.Passphrase, classifyLockSecret("correct1"))
        assertNull(classifyLockSecret("short"))
    }
}

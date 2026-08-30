package org.aetherfeed.app.boarddedup

import java.security.MessageDigest

fun fileDigest(bytes: ByteArray): String {
    val hash = MessageDigest.getInstance("SHA-256").digest(bytes)
    return hash.joinToString("") { byte -> "%02x".format(byte) }
}

package org.aetherfeed.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class ModeActionBus {
    var prev: (() -> Unit)? = null
    var next: (() -> Unit)? = null
    var play: (() -> Unit)? = null
    var skipBack: (() -> Unit)? = null
    var skipFwd: (() -> Unit)? = null
    var favorite: (() -> Unit)? = null
    var info: (() -> Unit)? = null
    var share: (() -> Unit)? = null
    var star: (() -> Unit)? = null
    var toggleRead: (() -> Unit)? = null
    var starred: Boolean = false
    var isRead: Boolean = false
    var playing: Boolean = false
    var revision by mutableIntStateOf(0)
        private set

    fun publish() {
        revision += 1
    }

    fun clearNews() {
        prev = null
        next = null
        star = null
        toggleRead = null
        starred = false
        isRead = false
        publish()
    }

    fun clearPodcasts() {
        play = null
        skipBack = null
        skipFwd = null
        playing = false
        publish()
    }

    fun clearBoards() {
        favorite = null
        info = null
        publish()
    }
}

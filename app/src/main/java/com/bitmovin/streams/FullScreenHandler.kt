package com.bitmovin.streams

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.setValue
import com.bitmovin.player.api.ui.FullscreenHandler

class FullScreenHandler(var fullscreen: MutableState<Boolean>) : FullscreenHandler {
    override var isFullscreen: Boolean = false
    override fun onDestroy() {
        TODO("Not yet implemented")
    }

    override fun onFullscreenExitRequested() {
        isFullscreen = false
        fullscreen.value = false
        handleFullScreen(false)
    }

    override fun onFullscreenRequested() {
        isFullscreen = true
        fullscreen.value = true
        handleFullScreen(true)
    }

    private fun handleFullScreen(b: Boolean) {
        doSystemUiVisibility(b)

    }

    override fun onPause() {
    }

    override fun onResume() {
        if (isFullscreen) {
            doSystemUiVisibility(true)
        }
    }

    private fun doSystemUiVisibility(fullscreen : Boolean) {
        // ...
    }
}
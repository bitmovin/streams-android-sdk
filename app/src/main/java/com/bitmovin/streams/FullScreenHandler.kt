package com.bitmovin.streams
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.ui.FullscreenHandler


class FullScreenHandler(val player : Player, var fullscreen: MutableState<Boolean> = mutableStateOf(false)) : FullscreenHandler {
    override fun onDestroy() {
        fullscreen.value = false
    }

    override fun onFullscreenExitRequested() {
        fullscreen.value = false
    }

    override fun onFullscreenRequested() {
        fullscreen.value = true
    }
    override fun onPause() {
        Log.d("FullScreenHandler", "onPause")
        fullscreen.value = false
    }

    override fun onResume() {
        Log.d("FullScreenHandler", "onResume")
        fullscreen.value = false
    }

    override val isFullscreen: Boolean
        get() = fullscreen.value
}

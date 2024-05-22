package com.bitmovin.streams
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bitmovin.player.api.ui.FullscreenHandler


class FullScreenHandler(val context: Context, var fullscreen: MutableState<Boolean> = mutableStateOf(false)) : FullscreenHandler {
    override fun onDestroy() {
        TODO("Not yet implemented")
    }

    override fun onFullscreenExitRequested() {
        fullscreen.value = false
    }

    override fun onFullscreenRequested() {
        fullscreen.value = true
    }
    override fun onPause() {
        TODO("Not yet implemented")
    }

    override fun onResume() {
        TODO("Not yet implemented")
    }

    override val isFullscreen: Boolean
        get() = fullscreen.value
}

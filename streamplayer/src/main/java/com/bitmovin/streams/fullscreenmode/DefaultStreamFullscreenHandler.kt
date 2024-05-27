package com.bitmovin.streams.fullscreenmode
import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.ui.FullscreenHandler
import java.lang.IndexOutOfBoundsException


class DefaultStreamFullscreenHandler(val playerView: PlayerView, val activity: Activity?, var fullscreen: MutableState<Boolean> = mutableStateOf(false)) : FullscreenHandler {

    override fun onDestroy() {
        Log.d("FullScreenHandler", "onDestroy")
        fullscreen.value = false
    }


    override fun onFullscreenExitRequested() {
        Log.d("FullScreenHandler", "onFullscreenExitRequested")
        fullscreen.value = false
    }

    override fun onFullscreenRequested() {
        Log.d("FullScreenHandler", "onFullscreenRequested")
        fullscreen.value = true
    }
    override fun onPause() {
        Log.d("FullScreenHandler", "onPause")
    }

    override fun onResume() {
        Log.d("FullScreenHandler", "onResume")
    }

    override val isFullscreen: Boolean
        get() = fullscreen.value
}

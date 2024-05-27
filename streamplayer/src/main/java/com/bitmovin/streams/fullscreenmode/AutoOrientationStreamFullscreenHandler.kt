package com.bitmovin.streams.fullscreenmode

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.ui.FullscreenHandler
import java.lang.IndexOutOfBoundsException

class AutoOrientationStreamFullscreenHandler(val playerView: PlayerView, val activity: Activity?, var defaultScreenOrientation : Int?) : FullscreenHandler {
    companion object {
        const val FORCE_PORTRAIT_RATIO = 0.9
        const val FORCE_LANDSCAPE_RATIO = 1.2
    }

    private var fullscreen: MutableState<Boolean> = mutableStateOf(false)
    private var previousOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    override fun onDestroy() {
        fullscreen.value = false
    }

    override fun onFullscreenExitRequested() {
        if (fullscreen.value) {
            activity?.requestedOrientation = defaultScreenOrientation ?: previousOrientation
            // Guarantee that the orientation is set before exiting fullscreen (100 ms is not noticeable by the user)
            Thread.sleep(100)
            fullscreen.value = false
        }
    }

    override fun onFullscreenRequested() {
        // Store the user orientation to restore it when exiting fullscreen
        if (!fullscreen.value) {
            previousOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            fullscreen.value = true
            var ratio: Float? = null
            try {
                ratio = playerView.player?.source?.availableVideoQualities?.get(0)
                    .let { it?.width?.toFloat()?.div(it.height) }
            } catch (e: IndexOutOfBoundsException) {

            }

            if (ratio != null && ratio < FORCE_PORTRAIT_RATIO) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
            } else if (ratio != null && ratio > FORCE_LANDSCAPE_RATIO) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
            } else if (ratio == null) {
                // Set to default orientation if ratio is not available
                activity?.requestedOrientation = defaultScreenOrientation ?: previousOrientation
            } else {
                // Stick with the user configuration
            }
        }
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

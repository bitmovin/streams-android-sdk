package com.bitmovin.streams.fullscreenmode

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.ui.FullscreenHandler
import com.bitmovin.streams.MAX_FOR_PORTRAIT_FORCING
import com.bitmovin.streams.MIN_FOR_LANDSCAPE_FORCING
import java.lang.IndexOutOfBoundsException

class AutoOrientationStreamFullscreenHandler(val playerView: PlayerView, val activity: Activity?, var defaultScreenOrientation : Int?) : FullscreenHandler {


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
        //doVisibilityFlags(false)
    }

    /*
    Does not work better than my current impl.
     */
    private fun doVisibilityFlags(fullscreen: Boolean) {
        val uiParams = getSystemUiVisibilityFlags(fullscreen, false)
        activity?.window?.decorView?.systemUiVisibility = uiParams
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

            if (ratio != null && ratio < MAX_FOR_PORTRAIT_FORCING) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            } else if (ratio != null && ratio > MIN_FOR_LANDSCAPE_FORCING) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else if (ratio == null) {
                // Set to default orientation if ratio is not available
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                // Stick with the user configuration
            }
        }
        //doVisibilityFlags(true)
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

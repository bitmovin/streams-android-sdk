package com.bitmovin.streams
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.ui.FullscreenHandler
import kotlinx.coroutines.delay
import okhttp3.internal.wait
import java.lang.IndexOutOfBoundsException


internal class FullScreenHandler(val viewModelStream: ViewModelStream, val activity: Activity?, var fullscreen: MutableState<Boolean> = mutableStateOf(false)) : FullscreenHandler {

    companion object {
        const val FORCE_PORTRAIT_RATIO = 0.9
        const val FORCE_LANDSCAPE_RATIO = 1.2
    }
    override fun onDestroy() {
        fullscreen.value = false
    }

    private var oldOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    override fun onFullscreenExitRequested() {
        activity?.requestedOrientation = oldOrientation
        fullscreen.value = false
    }

    override fun onFullscreenRequested() {
        // Store the user orientation to restore it when exiting fullscreen
        if (!fullscreen.value)
            oldOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        var ratio : Float? = null
        try {
            ratio = viewModelStream.player?.source?.availableVideoQualities?.get(0)
                .let { it?.width?.toFloat()?.div(it.height) }
        } catch (e : IndexOutOfBoundsException) {

        }


            if (ratio != null && ratio < FORCE_PORTRAIT_RATIO) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else if (ratio != null && ratio > FORCE_LANDSCAPE_RATIO) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else if (ratio == null) {
                // Supposing this is the default behavior if the ratio is not available
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                // Stick with the user configuration
            }
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
package com.bitmovin.streams

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.MutableState
import com.bitmovin.player.api.ui.FullscreenHandler


class FullScreenHandler(var fullscreen: MutableState<Boolean>, val context: Context) : FullscreenHandler {
    override fun onDestroy() {
        TODO("Not yet implemented")
    }

    override fun onFullscreenExitRequested() {
        fullscreen.value = false
        handleFullScreen(false)
    }

    override fun onFullscreenRequested() {
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

    override val isFullscreen: Boolean
        get() = fullscreen.value

    /**
     * This method toggles the system UI visibility.
     * cf : hide the status bar and the navigation bar
     */
    private fun doSystemUiVisibility(fullscreen: Boolean) {
        val activity = context.getActivity()
        if (activity == null) {
            Log.e("StreamsPlayer", "Activity not found")
            return
        }

        //TODO: For the Immersive full screen mode, this will be needed, but for now it's useless



    }
}

/**
 * Getting the Activity from a random Context
 */
fun Context.getActivity(): Activity? {
    val context = this
    if (context is ContextWrapper) {
        return if (context is Activity) {
            context
        } else {
            context.baseContext.getActivity()
        }
    }
    return null
}

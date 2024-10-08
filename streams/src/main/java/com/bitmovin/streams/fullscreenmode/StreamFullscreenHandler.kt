package com.bitmovin.streams.fullscreenmode

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.ui.FullscreenHandler
import com.bitmovin.streams.config.FullscreenConfig
import java.lang.IndexOutOfBoundsException

internal class StreamFullscreenHandler(
    val playerView: PlayerView,
    val activity: Activity?,
    val config: FullscreenConfig,
) : FullscreenHandler {
    private var fullscreen: MutableState<Boolean> = mutableStateOf(false)
    private var previousOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    override fun onDestroy() {
        fullscreen.value = false
    }

    override fun onFullscreenExitRequested() {
        if (fullscreen.value) {
            doOrientationChanges(false)
            fullscreen.value = false
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun doOrientationChanges(fullscreen: Boolean) {
        when (fullscreen) {
            true -> {
                var ratio: Float? = null
                if (config.autoRotate) {
                    previousOrientation = activity?.requestedOrientation
                        ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    try {
                        ratio =
                            playerView.player?.source?.availableVideoQualities?.get(0)
                                .let { it?.width?.toFloat()?.div(it.height) }
                    } catch (e: IndexOutOfBoundsException) {
                        // Do nothing, it is not a big deal if the ratio is null
                    }

                    if (ratio != null && ratio < config.minAspectRatioForLandscapeForce) {
                        activity?.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    } else if (ratio != null && ratio > config.maxAspectRatioForPortraitForce) {
                        activity?.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    } else if (ratio == null) {
                        // Set to default orientation if ratio is not available
                        activity?.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    } else {
                        // Stick with the user configuration
                    }
                }
            }

            false -> {
                activity?.requestedOrientation =
                    config.screenDefaultOrientation ?: previousOrientation
            }
        }
    }

    override fun onFullscreenRequested() {
        // Store the user orientation to restore it when exiting fullscreen
        if (!fullscreen.value) {
            doOrientationChanges(true)
            fullscreen.value = true
        }
    }

    override fun onPause() {
        // Nothing to do
    }

    override fun onResume() {
        // Nothing to do
    }

    override val isFullscreen: Boolean
        get() = fullscreen.value
}

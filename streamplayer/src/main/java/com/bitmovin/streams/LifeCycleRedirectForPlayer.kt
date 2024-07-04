package com.bitmovin.streams

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.bitmovin.player.PlayerView

internal class LifeCycleRedirectForPlayer(
    val playerView: PlayerView,
    private val autoPiP: Boolean,
) : LifecycleEventObserver {
    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event,
    ) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                playerView.onResume()
            }

            Lifecycle.Event.ON_STOP -> {
                playerView.onPause()
            }

            Lifecycle.Event.ON_DESTROY -> {
                playerView.onDestroy()
            }

            Lifecycle.Event.ON_RESUME -> {
                playerView.onResume()
            }

            Lifecycle.Event.ON_PAUSE -> {
                playerView.onPause()
                if (autoPiP && playerView.isFullscreen && playerView.player?.isPlaying == true) {
                    playerView.enterPictureInPicture()
                } else {
                    playerView.player?.pause()
                }
            }

            else -> {}
        }
    }
}

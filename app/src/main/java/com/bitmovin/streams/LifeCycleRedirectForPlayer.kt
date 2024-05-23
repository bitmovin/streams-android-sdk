package com.bitmovin.streams

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.bitmovin.player.PlayerView

internal class LifeCycleRedirectForPlayer(
    val playerView : PlayerView,
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                Log.d("LifeCycleStreams", "ON_START")
                playerView.onResume()
            }
            Lifecycle.Event.ON_STOP -> {
                Log.d("LifeCycleStreams", "ON_STOP")
                playerView.onPause()
            }
            Lifecycle.Event.ON_DESTROY -> {
                Log.d("LifeCycleStreams", "ON_DESTROY")
                playerView.onDestroy()
            }

            Lifecycle.Event.ON_RESUME -> {
                Log.d("LifeCycleStreams", "ON_RESUME")
                playerView.onResume()
            }
            Lifecycle.Event.ON_PAUSE -> {
                Log.d("LifeCycleStreams", "ON_PAUSE")
                playerView.onPause()
            }
            else -> {}
        }
    }

}
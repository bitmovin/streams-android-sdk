package com.bitmovin.streams.pipmode

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.bitmovin.streams.getActivity

internal class PiPChangesObserver() : LifecycleEventObserver {
    var context: Context? = null

    private val listeners: MutableSet<PiPExitListener> = mutableSetOf()
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_PAUSE) {

            val activity = context?.getActivity()
            val isInPipMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                activity?.isInPictureInPictureMode ?: false
            } else {
                false
            }
            val newConfig = context?.resources?.configuration ?: Configuration()
            onPictureInPictureModeChanged(isInPipMode, newConfig)
        }
    }

    fun addListener(listener: PiPExitListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: PiPExitListener) {
        listeners.remove(listener)
    }

    private fun onPictureInPictureModeChanged(inPipMode: Boolean, newConfig: Configuration) {
        Log.d("StreamPlayer", "onPictureInPictureModeChanged: $inPipMode $this")
        if (!inPipMode) {
            listeners.forEach { if (it.isInPiPMode()) {
                it.onPiPExit()
            } }
        }
    }
}
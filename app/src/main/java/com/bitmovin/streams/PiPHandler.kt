package com.bitmovin.streams

import android.app.Activity
import com.bitmovin.player.api.Player
import com.bitmovin.player.ui.DefaultPictureInPictureHandler
import java.util.UUID

class PiPHandler(val viewModelStream: ViewModelStream, activity: Activity?, player: Player?) : DefaultPictureInPictureHandler(activity,
    player
) {
    companion object {
        var currentPiP: String? = null
    }
    private val id = UUID.randomUUID().toString()
    private var oldFullScreen = viewModelStream.isFullScreen.value
    private var oldImmersiveFullScreen = viewModelStream.immersiveFullScreen
    override fun enterPictureInPicture() {
        currentPiP = id
        oldFullScreen = viewModelStream.isFullScreen.value
        oldImmersiveFullScreen = viewModelStream.immersiveFullScreen
        viewModelStream.immersiveFullScreen = false
        viewModelStream.isFullScreen.value = true
        super.enterPictureInPicture()
    }

    override fun exitPictureInPicture() {
        if (PiPHandler.currentPiP == id) {
            viewModelStream.isFullScreen.value = oldFullScreen
            viewModelStream.immersiveFullScreen = oldImmersiveFullScreen
            PiPHandler.currentPiP = null
            super.exitPictureInPicture()
        }
    }
}
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
    val id = UUID.randomUUID().toString()
    private var oldFullScreen = viewModelStream.isFullScreen.value
    private var oldImmersiveFullScreen = viewModelStream.immersiveFullScreen
    override fun enterPictureInPicture() {
        super.enterPictureInPicture()
        currentPiP = id
        oldFullScreen = viewModelStream.isFullScreen.value
        oldImmersiveFullScreen = viewModelStream.immersiveFullScreen
        // Being in immersive full screen mode cause the PiP to sometimes need another recomposition to be displayed correctly, so we just avoid it
        viewModelStream.immersiveFullScreen = false
        // The full screen mode is needed to display the PiP nicely and borderless
        viewModelStream.isFullScreen.value = true
        viewModelStream.playerView?.isUiVisible = false

    }

    override fun exitPictureInPicture() {
        super.exitPictureInPicture()
        // Because of my impl, but should be refactored for a better separation of concerns
        if (PiPHandler.currentPiP == id) {
            // Restore the previous values
            viewModelStream.isFullScreen.value = oldFullScreen
            viewModelStream.immersiveFullScreen = oldImmersiveFullScreen
            PiPHandler.currentPiP = null
            viewModelStream.playerView?.isUiVisible = true

        }
    }
}
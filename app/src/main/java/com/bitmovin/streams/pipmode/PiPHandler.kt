package com.bitmovin.streams.pipmode

import android.app.Activity
import com.bitmovin.player.api.Player
import com.bitmovin.player.ui.DefaultPictureInPictureHandler
import com.bitmovin.streams.ViewModelStream

internal class PiPHandler(val viewModelStream: ViewModelStream, activity: Activity?, player: Player?) : DefaultPictureInPictureHandler(activity,
    player
) {

    private var oldFullScreen = viewModelStream.isFullScreen.value
    private var oldImmersiveFullScreen = viewModelStream.immersiveFullScreen
    private var isInPictureInPicture = false

    override fun enterPictureInPicture() {
        super.enterPictureInPicture()
        isInPictureInPicture = true
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
        // Restore the previous values
        isInPictureInPicture = false
        viewModelStream.isFullScreen.value = oldFullScreen
        viewModelStream.immersiveFullScreen = oldImmersiveFullScreen
        viewModelStream.playerView?.isUiVisible = true
    }

    override val isPictureInPicture: Boolean
        get() = isInPictureInPicture


}
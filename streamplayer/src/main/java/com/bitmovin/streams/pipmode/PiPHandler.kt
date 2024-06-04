package com.bitmovin.streams.pipmode

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.player.ui.DefaultPictureInPictureHandler
import com.bitmovin.streams.ViewModelStream

internal class PiPHandler(activity: Activity?, private val playerView: PlayerView, var immersiveFullScreen : MutableState<Boolean>) : DefaultPictureInPictureHandler(activity,
    playerView.player
) {

    private var isInPictureInPicture = false
    private var oldImmersiveFullScreen = immersiveFullScreen.value
    private var previousUiVisibility = true

    override fun enterPictureInPicture() {
        super.enterPictureInPicture()
        isInPictureInPicture = true
        oldImmersiveFullScreen = immersiveFullScreen.value

        previousUiVisibility = playerView.isUiVisible
        playerView.isUiVisible = false

//         Being in immersive full screen mode cause the PiP to sometimes need another recomposition to be displayed correctly, so we just avoid it
//         The full screen mode is needed to display the PiP nicely and borderless
//         Wait depending of the SDK used (some devices need more time to enter PiP mode than others
//        if (Build.VERSION.SDK_INT <= 29) {
//            Thread.sleep(500)
//        }
        immersiveFullScreen.value = false
        playerView.enterFullscreen()

    }

    override fun exitPictureInPicture() {
        immersiveFullScreen.value = oldImmersiveFullScreen
        // Restore the previous values
        isInPictureInPicture = false
        //playerView.exitFullscreen()
        playerView.isUiVisible = previousUiVisibility
        super.exitPictureInPicture()
    }

    override val isPictureInPicture: Boolean
        get() = isInPictureInPicture


}
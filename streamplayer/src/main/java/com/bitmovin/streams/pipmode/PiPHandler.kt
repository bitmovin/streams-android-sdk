package com.bitmovin.streams.pipmode

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import com.bitmovin.player.PlayerView
import com.bitmovin.player.ui.DefaultPictureInPictureHandler
import com.bitmovin.streams.Tag

internal class PiPHandler(activity: Activity?, private val playerView: PlayerView) : DefaultPictureInPictureHandler(activity,
    playerView.player
) {

    private var isInPictureInPicture = false
    private var previousUiVisibility = true

    override fun enterPictureInPicture() {
        Log.d(Tag.Stream, "enterPictureInPicture")
        super.enterPictureInPicture()
        isInPictureInPicture = true

        previousUiVisibility = playerView.isUiVisible
        playerView.isUiVisible = false

//         Being in immersive full screen mode cause the PiP to sometimes need another recomposition to be displayed correctly, so we just avoid it
//         The full screen mode is needed to display the PiP nicely and borderless
//         Wait depending of the SDK used (some devices need more time to enter PiP mode than others
//        if (Build.VERSION.SDK_INT <= 29) {
//            Thread.sleep(500)
//        }
        playerView.enterFullscreen()

    }

    override fun exitPictureInPicture() {
        Log.d(Tag.Stream, "exitPictureInPicture")
        // Restore the previous values
        isInPictureInPicture = false
        playerView.isUiVisible = previousUiVisibility
        super.exitPictureInPicture()
    }

    override val isPictureInPicture: Boolean
        get() = isInPictureInPicture


}
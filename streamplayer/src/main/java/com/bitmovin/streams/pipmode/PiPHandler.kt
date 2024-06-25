package com.bitmovin.streams.pipmode

import android.app.Activity
import android.util.Log
import com.bitmovin.player.PlayerView
import com.bitmovin.player.ui.DefaultPictureInPictureHandler
import com.bitmovin.streams.Tag

internal class PiPHandler(activity: Activity?, private val playerView: PlayerView) :
    DefaultPictureInPictureHandler(
        activity,
        playerView.player
    ) {

    private var isInPictureInPicture = false
    private var previousUiVisibility = true

    override fun enterPictureInPicture() {
        super.enterPictureInPicture()
        isInPictureInPicture = true
        previousUiVisibility = playerView.isUiVisible
        playerView.isUiVisible = false
        playerView.enterFullscreen()

    }

    override fun exitPictureInPicture() {
        // Restore the previous values
        isInPictureInPicture = false
        playerView.isUiVisible = previousUiVisibility
        super.exitPictureInPicture()
    }

    override val isPictureInPicture: Boolean
        get() = isInPictureInPicture


}
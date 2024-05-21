package com.bitmovin.streams

import android.annotation.TargetApi
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Intent
import android.os.Build.VERSION
import android.util.Log
import android.util.Rational
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.ui.PictureInPictureHandler
import kotlin.jvm.internal.Intrinsics


@TargetApi(26)
class DefaultPiPHandlerDeobs(activity: Activity, player: Player) :
    PictureInPictureHandler {
    private val activity: Activity
    private val player: Player

    init {
        Intrinsics.checkNotNull(activity)
        Intrinsics.checkNotNull(player)
        this.activity = activity
        this.player = player
    }

    override val isPictureInPicture: Boolean
        get() {
            val currentActivity = this.activity
            return currentActivity.isInPictureInPictureMode
        }

    override val isPictureInPictureAvailable: Boolean
        get() = VERSION.SDK_INT >= 26

    override fun enterPictureInPicture() {
        if (!this.isPictureInPictureAvailable) {
            Log.w("PiPHandler","Calling DefaultPictureInPictureHandler.enterPictureInPicture without PiP support.")
        } else if (!this.isPictureInPicture) {
            val aspectRatio =
                player.source?.selectedVideoQuality?.let { Rational(it.width, it.height) }
            val params = PictureInPictureParams.Builder().setAspectRatio(aspectRatio).build()
            activity.enterPictureInPictureMode(params)
        }
    }

    override fun exitPictureInPicture() {
        if (!this.isPictureInPictureAvailable) {
            Log.w("PiPHandler", "Calling DefaultPictureInPictureHandler.exitPictureInPicture without PiP support.")
        } else if (this.isPictureInPicture) {
            val intent = Intent(
                this.activity,
                activity.javaClass
            )
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            activity.startActivity(intent)
        }
    }
}
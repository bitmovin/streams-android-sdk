package com.bitmovin.testapp.utils

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.streams.BitmovinStream
import com.bitmovin.streams.config.BitmovinStreamEventListener
import com.bitmovin.streams.config.FullscreenConfig
import com.bitmovin.streams.config.PlayerThemes
import com.bitmovin.streams.config.StyleConfigStream
import com.bitmovin.testapp.ui.theme.StreamsandroidsdkTheme
import kotlin.math.abs

class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        val streamId = intent.getStringExtra("streamId") ?: ""
        val token = intent.getStringExtra("token")
        enableEdgeToEdge()
        setContent {
            StreamsandroidsdkTheme {
                Player(streamId, token)
            }
        }
    }

    @Composable
    fun Player(streamId: String, token: String?) {
        var name by remember { mutableStateOf("Loading...") }
        var description by remember { mutableStateOf("Loading...") }
        val config = LocalConfiguration.current

        var aspectRatio by remember { mutableFloatStateOf(16f / 9f) }
        // not really useful but it looks cleaner.
        val aspectRatioAnim by animateFloatAsState(targetValue = aspectRatio, label = "Video Aspect Ratio Anim",)
        val activity = LocalContext.current as Activity
        var playerViewHolder by remember { mutableStateOf<PlayerView?>(null) }

        // Simple example of how to use OrientationEventListener to force landscape mode
        remember { mutableStateOf<OrientationEventListener?>(
        object : OrientationEventListener(activity) {
            var AUTO_ROTATE_STATE = AutoRotateState.WaitingForEnter
            override fun onOrientationChanged(orientation: Int) {

                if (aspectRatio <= 0.8f) return
                when (AUTO_ROTATE_STATE) {
                    AutoRotateState.WaitingForEnter -> {
                        if (playerViewHolder?.isFullscreen == false && (abs(orientation - 90) < FS_TREESHOLD || abs(orientation - 270) < FS_TREESHOLD)) {
                            AUTO_ROTATE_STATE = AutoRotateState.WaitingForExit
                            playerViewHolder?.enterFullscreen()
                        }
                    }
                    AutoRotateState.WaitingForExit -> {
                        if (playerViewHolder?.isFullscreen == false) {
                            AUTO_ROTATE_STATE = AutoRotateState.WaitingForReset
                        } else {
                            if (abs(orientation - 0) < FS_TREESHOLD || abs(orientation - 360) < FS_TREESHOLD || abs(orientation - 180) < FS_TREESHOLD) {
                                playerViewHolder?.exitFullscreen()
                                AUTO_ROTATE_STATE = AutoRotateState.WaitingForEnter
                            }
                        }

                    }
                    AutoRotateState.WaitingForReset -> {
                        if (abs(orientation - 0) < FS_TREESHOLD || abs(orientation - 360) < FS_TREESHOLD || abs(orientation - 180) < FS_TREESHOLD){
                            AUTO_ROTATE_STATE = AutoRotateState.WaitingForEnter
                        }
                    }
                }
                Log.d("PlayerActivity", "Orientation: $orientation, State $AUTO_ROTATE_STATE")
            }

        }.apply { enable() }
        )}

        Column(Modifier.safeDrawingPadding()) {
            BitmovinStream(
                streamId = streamId,
                modifier = Modifier
                    .aspectRatio(aspectRatioAnim)
                    .fillMaxHeight(0.7f),
                jwToken = token,
                fullscreenConfig = FullscreenConfig(
                    screenDefaultOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                ),
                bitmovinStreamEventListener = object : BitmovinStreamEventListener {
                    override fun onPlayerReady(player: Player) {
                        name = player.source?.config?.title ?: "Unknown"
                        description = player.source?.config?.description ?: "None"
                        // Launch a separated thread that will search for videoQualities until it find at least one
                        Thread {
                            var attempts = 0
                            while (attempts < 30) {
                                Thread.sleep(75)
                                if (player.source?.availableVideoQualities?.isNotEmpty() == true) {
                                    break
                                }
                                attempts++
                            }
                            val videoQualities = player.source?.availableVideoQualities
                            if (!videoQualities.isNullOrEmpty()) {
                                aspectRatio =
                                    videoQualities[0].width.toFloat() / videoQualities[0].height.toFloat()
                            } else {
                                Log.e("PlayerActivity", "No video qualities found")
                            }
                        }.start()
                    }

                    override fun onPlayerViewReady(playerView: PlayerView) {
                        playerViewHolder = playerView
                        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            if (!playerView.isFullscreen)
                                playerView.enterFullscreen()
                        }
                    }
                },
                styleConfig = PlayerThemes.RED_EXAMPLE_THEME
            )
            Text(
                text = name, Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
            Text(text = description, Modifier.padding(8.dp))
        }
    }

    companion object {
        const val FS_TREESHOLD = 8
    }
}

enum class AutoRotateState {
    WaitingForExit,
    WaitingForEnter,
    WaitingForReset
}
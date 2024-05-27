package com.bitmovin.testapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap.Config
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.bitmovin.streams.BitmovinStreamEventListener
import com.bitmovin.testapp.ui.theme.StreamsandroidsdkTheme
import kotlin.math.abs

class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val streamId = intent.getStringExtra("streamId") ?: ""
        enableEdgeToEdge()
        setContent {
            StreamsandroidsdkTheme {
                Player(streamId)
            }
        }

        val list = object : OrientationEventListener(this) {
            val activity = this@PlayerActivity
            var last_mode = 0
            val ROTATION_THRESHOLD = 8
            override fun onOrientationChanged(orientation: Int) {
                when {
                    abs(orientation - 0) < ROTATION_THRESHOLD -> {
                        changeMode(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    }
                    abs(orientation - 90) < ROTATION_THRESHOLD -> {
                        changeMode(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                    }
                    abs(orientation - 180) < ROTATION_THRESHOLD -> {
                        changeMode(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
                    }
                    abs(orientation - 270) < ROTATION_THRESHOLD -> {
                        changeMode(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    }
                }
            }
            fun changeMode (mode: Int) {
                if (last_mode != mode) {
                    activity.requestedOrientation = mode
                    last_mode = mode
                }
            }
        }.apply { enable() }
    }
}

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun Player(streamId: String, modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("Loading...") }
    var description by remember { mutableStateOf("Loading...") }
    val config = LocalConfiguration.current
    var aspectRatio by remember { mutableFloatStateOf(1.0f) }
    val activity = LocalContext.current as Activity
    var playerViewHolder by remember { mutableStateOf<PlayerView?>(null) }
    LocalConfiguration.current.orientation
    key (activity.requestedOrientation) {
        Log.d("PlayerActivity", "Orientation: ${config.orientation}, ${activity.requestedOrientation}")
        playerViewHolder?.let {
            if (aspectRatio > 0.9f) {
                if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (!it.isFullscreen) {
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                        it.enterFullscreen()
                    }
                } else {
                    if (it.isFullscreen)
                        it.exitFullscreen()
                }
            }
        }
    }
    Column(Modifier) {
        BitmovinStream(
            streamId = streamId,
            modifier = Modifier
                .aspectRatio(aspectRatio)
                .fillMaxHeight(0.7f),
            immersiveFullScreen = true,
            bitmovinStreamEventListener = object : BitmovinStreamEventListener {
                override fun onPlayerReady(player: Player) {
                    name = player.source?.config?.title ?: "Unknown"
                    description = player.source?.config?.description ?: "None"
                    // Launch a separated thread that will search for videoQualities until it find at least one
                    Thread {
                        var attempts = 0
                        while (attempts < 25) {
                            Thread.sleep(50)
                            if (player.source?.availableVideoQualities?.isNotEmpty() == true) {
                                break
                            }
                            attempts++
                        }
                        val videoQualities = player.source?.availableVideoQualities
                        if (!videoQualities.isNullOrEmpty()) {
                            aspectRatio = videoQualities[0].width.toFloat() / videoQualities[0].height.toFloat()
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
            }
        )
        Text(
            text = name, Modifier.padding(8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
            )
        Text(text = description, Modifier.padding(8.dp))
    }
}
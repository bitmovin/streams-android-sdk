package com.bitmovin.testapp

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.streams.BitmovinStream
import com.bitmovin.streams.config.StreamError
import com.bitmovin.streams.config.StreamListener
import com.bitmovin.streams.config.StyleConfigStream
import com.bitmovin.testapp.ui.theme.LightColorScheme
import com.bitmovin.testapp.ui.theme.StreamsandroidsdkTheme
import com.bitmovin.testapp.utils.PlayerActivity
import com.bitmovin.testapp.utils.TestStreamsIds

class StreamLibraryApp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContent {
            StreamsandroidsdkTheme {
                StreamsList()
            }
        }
    }
}

@Composable
fun StreamsList() {
    val unfoldedStreamId = remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        StreamElem("Sintel", TestStreamsIds.SINTEL, unfoldedStreamId)
        StreamElem(
            name = "Big Buck Bunny",
            streamId = TestStreamsIds.BIG_BUCK_BUNNY,
            unfoldedStreamId,
        )
        StreamElem(name = "Squared Video", streamId = TestStreamsIds.SQUARE_VIDEO, unfoldedStreamId)
        StreamElem("Vertical Video", TestStreamsIds.VERTICAL_VIDEO, unfoldedStreamId)
    }
}

@Composable
fun StreamElem(
    name: String,
    streamId: String,
    unfoldedStreamId: MutableState<String?>,
    token: String? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var playerHolder: Player? by remember { mutableStateOf(null) }
    val isVisible = playerHolder != null

    val bgColor =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(LocalContext.current).background
        } else {
            LightColorScheme.background
        }
    val streamHeight by animateDpAsState(
        targetValue = if (unfoldedStreamId.value == streamId) 200.dp else 0.dp,
        label = "Unfolding Anim",
    )
    // Rounded corner 8dp
    val buttonColor =
        when (unfoldedStreamId.value) {
            streamId -> Color(0, 106, 237)
            null -> Color.Gray
            else -> bgColor
        }
    Column(
        Modifier
            .background(color = buttonColor)
            .clickable {
                if (isVisible) {
                    if (unfoldedStreamId.value != streamId) {
                        // Show the stream preview
                        playerHolder?.play()
                        unfoldedStreamId.value = streamId
                    } else {
                        // Trigger the action
                        switchToPlayerActivity(streamId, context, token)
                    }
                }
            }
            .padding(8.dp),
    ) {
        var text by remember { mutableStateOf("Loading...") }
        Text(
            text = name,
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp),
            color = Color.Black,
            fontSize = 24.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
        if (!isVisible) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp),
                color = Color.DarkGray,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(streamHeight),
        ) {
            BitmovinStream(
                streamId = streamId,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color(0, 106, 237)),
                subtitles = emptyList(),
                enableAds = false,
                startTime = 5.0,
                muted = true,
                authenticationToken = token,
                streamListener =
                    object : StreamListener {
                        override fun onStreamReady(
                            player: Player,
                            playerView: PlayerView,
                        ) {
                            playerHolder = player
                            playerView.isUiVisible = false
                        }

                        override fun onStreamError(error: StreamError) {
                            text = error.toString()
                        }
                    },
                styleConfig =
                    StyleConfigStream(
                        backgroundColor = Color(0, 106, 237),
                    ),
            )
        }
    }
    if (unfoldedStreamId.value != streamId) {
        playerHolder?.pause()
    } else {
        playerHolder?.play()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StreamsandroidsdkTheme {
        StreamsList()
    }
}

fun switchToPlayerActivity(
    streamId: String,
    packageContext: Context,
    token: String?,
) {
    // Go to Activity PlayerActivity
    val intent = Intent(packageContext, PlayerActivity::class.java)
    intent.putExtra("streamId", streamId)
    intent.putExtra("token", token)
    packageContext.startActivity(intent)
}

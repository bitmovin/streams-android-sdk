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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.streams.BitmovinStream
import com.bitmovin.streams.config.BitmovinStreamEventListener
import com.bitmovin.streams.config.StyleConfigStream
import com.bitmovin.streams.TestStreamsIds
import com.bitmovin.testapp.ui.theme.LightColorScheme
import com.bitmovin.testapp.ui.theme.StreamsandroidsdkTheme
import com.bitmovin.testapp.utils.PlayerActivity

class TestApp2 : ComponentActivity() {
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
    var unfoldedStreamId = remember { mutableStateOf<String?>(null) }
    Column(modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()
    ) {
        StreamElem("Sintel", TestStreamsIds.SINTEL, unfoldedStreamId)
        StreamElem("Vertical Video", TestStreamsIds.VERTICAL_VIDEO, unfoldedStreamId)
        StreamElem(name = "Squared Video", streamId = TestStreamsIds.SQUARE_VIDEO, unfoldedStreamId)
        StreamElem (name = "Tears of Steel", streamId = TestStreamsIds.TEAR_OF_STEEL, unfoldedStreamId)
        StreamElem(
            name = "Big Buck Bunny - token required",
            streamId = TestStreamsIds.BIG_BUCK_BUNNY,
            unfoldedStreamId,
            token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MTg2OTY0NDB9.J7ysnY4jc6cHHSTbfoqz3PApo2WlO36pi94mU92MAAp77iDYQuMDtqcuGwdE7OBMSwkFvvpmLEJNgFh02Q3bcpiQWtQZaH43uObsQpJnpnoDSwghq3BWXo0_F478lPk51L1-F7UBpYjctNJ9usmJD-c9hCOmd-gTLmvjBx0Ytveh4PY6kWbNjahZT1sHu-SGDwxJJEgqrf18PXDb1tO9GHU6xIgLrXa956m9yaz9XMFPvN55C7SMmvGZxkSFDa_0WQssikZo4Xa4z14ZuNGv5JpiE4pP7zBj6Ll0ri9Ofypof_aw1DJiR5O6MP7sK7nYRgZR0MrlJ2OrOcBxYCqbnA"
        )
    }
}
@Composable
fun StreamElem(name: String, streamId: String, unfoldedStreamId: MutableState<String?>, token: String? = null, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var playerHolder: Player? by remember { mutableStateOf(null) }
    var isVisible = playerHolder != null

    val bgColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(LocalContext.current).background
    } else {
        LightColorScheme.background
    }
    val streamHeight by animateDpAsState(targetValue = if (unfoldedStreamId.value == streamId) 200.dp else 0.dp, label = "Unfolding Anim")
    // Rounded corner 8dp
    val buttonColor = when (unfoldedStreamId.value) {
        streamId -> Color(0, 106, 237)
        null -> Color.Gray
        else -> bgColor
    }
    Column(
        Modifier
            .background(color = buttonColor)
            .clickable {
                if (isVisible)
                    if (unfoldedStreamId.value != streamId) {
                        // Show the stream preview
                        playerHolder?.play()
                        unfoldedStreamId.value = streamId
                    } else {
                        // Trigger the action
                        switchToPlayerActivity(streamId, context, token)
                    }
            }
            .alpha(if (isVisible) 1f else 0.5f)
            .padding(8.dp))
    {
        Row {
            Text(text = name, modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp), color = Color.Black, fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            if (!isVisible)
                Text(text = "Loading...", modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp), color = Color.DarkGray, fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(streamHeight)

        ) {
            BitmovinStream(
                streamId = streamId,
                modifier = Modifier.fillMaxSize().background(Color(0, 106, 237)),
                subtitles = emptyList(),
                enableAds = false,
                start = 5.0,
                muted = true,
                jwToken = token,
                bitmovinStreamEventListener = object : BitmovinStreamEventListener {
                    override fun onPlayerReady(player: Player) {
                        playerHolder = player
                    }

                    override fun onPlayerViewReady(playerView: PlayerView) {
                        playerView.isUiVisible = false
                    }
                },
                styleConfig = StyleConfigStream(
                    backgroundColor = Color(0, 106, 237)
                )
            )
        }
    }
    if (unfoldedStreamId.value != streamId)
        playerHolder?.pause()
    else
        playerHolder?.play()
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StreamsandroidsdkTheme {
        StreamsList()
    }
}

fun switchToPlayerActivity(streamId: String, packageContext: Context, token: String?) {
    // Go to Activity PlayerActivity
    val intent = Intent(packageContext, PlayerActivity::class.java)
    intent.putExtra("streamId", streamId)
    intent.putExtra("token", token)
    packageContext.startActivity(intent)
}
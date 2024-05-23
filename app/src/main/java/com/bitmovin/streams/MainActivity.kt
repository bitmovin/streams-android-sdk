package com.bitmovin.streams

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.streams.ui.theme.StreamsandroidsdkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StreamsandroidsdkTheme {
                Column {

                    Box(modifier = Modifier
                        .size(400.dp)
                        .padding(40.dp)) {
                        Column {
                        Text(text = "Video 1")
                        BitmovinStream(
                            streamId = TEST_STREAMS_ID.VERTICAL_VIDEO,
                            subtitles = listOf(
                                SubtitleTrack(
                                    language = "francais",
                                    url = "https://cdn.bitmovin.com/content/assets/sintel/subtitles/subtitles_fr.vtt"
                                )
                            ),
                            modifier = Modifier,
                            immersiveFullScreen = true,
                            bitmovinStreamEventListener = object : BitmovinStreamEventListener {
                                override fun onPlayerReady(player: Player) {
                                    player.play()
                                }

                                override fun onPlayerViewReady(playerView: PlayerView) {
                                }

                            }
                        )

                        }
                    }
                    Box(modifier = Modifier
                        .size(400.dp)
                        .padding(40.dp)) {
                        Column {
                        Text(text = "Video 2")
                        BitmovinStream(
                            streamId = TEST_STREAMS_ID.SINTEL,
                            modifier = Modifier,
                            immersiveFullScreen = true
                        )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StreamsandroidsdkTheme {
        Greeting("Android")
    }
}

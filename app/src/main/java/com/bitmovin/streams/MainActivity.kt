package com.bitmovin.streams

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.streams.ui.theme.StreamsandroidsdkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StreamsandroidsdkTheme {
                Column {

                    Box(modifier = Modifier
                        .size(400.dp)
                        .padding(40.dp)) {
                        BitmovinStream(
                            streamId = "cp6uqgl100s5p8qijmug",
                            subtitles = listOf(
                                SubtitleTrack(
                                    language = "francais",
                                    url = "https://cdn.bitmovin.com/content/assets/sintel/subtitles/subtitles_fr.vtt"
                                )
                            ),
                            modifier = Modifier,
                            immersiveFullScreen = false
                        )
                    }
                    Box(modifier = Modifier
                        .size(400.dp)
                        .padding(40.dp)) {
                        BitmovinStream(
                            streamId = TEST_STREAMS_ID.SINTEL,
                            modifier = Modifier
                        )
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

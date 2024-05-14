package com.bitmovin.streams

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
        enableEdgeToEdge()
        setContent {
            StreamsandroidsdkTheme{
                StreamsPlayer(
                    streamId = TEST_STREAMS_ID.TEAR_OF_STEEL,
                    subtitles = listOf(
                        SubtitleTrack(language = "francais", url = "https://cdn.bitmovin.com/content/assets/sintel/subtitles/subtitles_fr.vtt"),
                        SubtitleTrack(language = "German", url = "https://cdn.bitmovin.com/content/assets/sintel/subtitles/subtitles_de.vtt"),
                        SubtitleTrack(label = "Spanish", url = "https://cdn.bitmovin.com/content/assets/sintel/subtitles/subtitles_es.vtt"),
                    ),
                    // Top padding and centered
                    modifier = Modifier.padding(top = 64.dp).padding(start = 16.dp, end = 16.dp)
                )
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
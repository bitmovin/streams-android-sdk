package com.bitmovin.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.streams.BitmovinStream
import com.bitmovin.streams.config.StyleConfigStream
import com.bitmovin.streams.TestStreamsIds

class DefaultTest : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Column {

                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(20.dp)
                ) {
                    Column {
                        Text(text = "Video 1")
                        BitmovinStream(
                            streamId = TestStreamsIds.VERTICAL_VIDEO,
                            subtitles = emptyList(),
                            modifier = Modifier,
                            styleConfig = StyleConfigStream(
                                customCss = """
                                            div {
                                              font-size: 15pt !important;
                                            }
                                        """.trimIndent()
                            )
                        )

                    }
                }

                BitmovinStream(
                    streamId = TestStreamsIds.SQUARE_VIDEO,
                    modifier = Modifier
                        .size(300.dp)
                        .padding(20.dp),
                    enableAds = false,
                    subtitles = listOf(
                        SubtitleTrack(
                            language = "French",
                            url = "https://cdn.bitmovin.com/content/assets/sintel/subtitles/subtitles_fr.vtt"
                        ),
                        SubtitleTrack(
                            language = "German",
                            url = "https://cdn.bitmovin.com/content/assets/sintel/subtitles/subtitles_de.vtt"
                        ),
                    )
                )
            }
        }
    }
}

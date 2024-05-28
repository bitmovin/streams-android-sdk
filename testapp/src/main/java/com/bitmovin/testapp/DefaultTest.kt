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
import com.bitmovin.streams.BitmovinStream
import com.bitmovin.streams.TestStreamsIds

class DefaultTest : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                Column {

                    Box(
                        modifier = Modifier
                            .size(400.dp)
                            .padding(40.dp)
                    ) {
                        Column {
                            Text(text = "Video 1")
                            BitmovinStream(
                                streamId = TestStreamsIds.VERTICAL_VIDEO,
                                subtitles = emptyList(),
                                modifier = Modifier,
                                immersiveFullScreen = true
                            )

                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(400.dp)
                            .padding(40.dp)
                    ) {
                        Column {
                            Text(text = "Video 2")
                            BitmovinStream(
                                streamId = TestStreamsIds.SINTEL,
                                modifier = Modifier,
                                immersiveFullScreen = true
                            )
                        }
                    }
                }
        }
    }
}

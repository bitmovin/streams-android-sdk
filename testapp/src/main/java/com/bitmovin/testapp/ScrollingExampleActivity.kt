package com.bitmovin.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bitmovin.streams.BitmovinStream
import com.bitmovin.streams.PlayerStyleConfigStream
import com.bitmovin.streams.StyleConfigStream
import com.bitmovin.streams.TestStreamsIds
import com.bitmovin.testapp.ui.theme.StreamsandroidsdkTheme

class ScrollingExampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StreamsandroidsdkTheme {
                // Scrollable column
                Column(modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()) {
                    // Draw image from drawable
                    Image(
                        painter = painterResource(id = R.drawable.carbon),
                        contentDescription = "Bitmovin Logo",
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    Box(modifier = Modifier.aspectRatio(16f / 9)) {
                        BitmovinStream(
                            streamId = TestStreamsIds.SQUARE_VIDEO
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
fun GreetingPreview2() {
    StreamsandroidsdkTheme {
        Greeting("Android")
    }
}
package com.bitmovin.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bitmovin.streams.BitmovinStream
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

                    Text(text = "Discovering the Bitmovin Stream Player Component... bla bla bla")
                    // Draw image from drawable
                    Image(
                        painter = painterResource(id = R.drawable.carbon),
                        contentDescription = "Bitmovin Logo",
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    BitmovinStream(
                        streamId = TestStreamsIds.SQUARE_VIDEO,
                        modifier = Modifier.aspectRatio(16f / 9)
                    )
                }
            }
        }
    }
}


@Composable
fun MdTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(16.dp)
    )
}
@Composable
fun MdText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(16.dp)
    )
}


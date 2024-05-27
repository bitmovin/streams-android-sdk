package com.bitmovin.testapp

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.bitmovin.streams.BitmovinStream
import com.bitmovin.streams.TestStreamsIds
import com.bitmovin.testapp.ui.theme.StreamsandroidsdkTheme

class TestApp2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContent {
            StreamsandroidsdkTheme {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()) {
                    StreamElem("Sintel", TestStreamsIds.SINTEL)
                    StreamElem("Vertical Video", TestStreamsIds.VERTICAL_VIDEO)
                    StreamElem(name = "Squared Video", streamId = TestStreamsIds.SQUARE_VIDEO)
                }
            }
        }
    }
}

@Composable
fun StreamElem(name: String, streamId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(Modifier.background(color = androidx.compose.ui.graphics.Color.Red)) {
        Button(
            onClick = { switchToPlayerActivity(streamId, context) },
            Modifier.fillMaxWidth()
        ){
            Text(text = name)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StreamsandroidsdkTheme {
        StreamElem("Sintel", "AAA")
    }
}

fun switchToPlayerActivity(streamId: String, packageContext: Context) {

    // Go to Activity PlayerActivity
    val intent = Intent(packageContext, PlayerActivity::class.java)
    intent.putExtra("streamId", streamId)
    packageContext.startActivity(intent)

}
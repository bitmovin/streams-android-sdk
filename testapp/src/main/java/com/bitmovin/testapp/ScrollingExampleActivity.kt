package com.bitmovin.testapp

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitmovin.streams.BitmovinStream
import com.bitmovin.streams.TestStreamsIds
import com.bitmovin.streams.config.StyleConfigStream
import com.bitmovin.testapp.ui.theme.StreamsandroidsdkTheme
import kotlinx.coroutines.delay

class ScrollingExampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StreamsandroidsdkTheme {
                BitmovinShowcase()
            }
        }
    }
}

@Composable
fun BitmovinShowcase() {
    val scrollState = rememberScrollState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        visible = true
    }

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxWidth()
            .padding(if (isLandscape) 144.dp else 10.dp, 0.dp)
            .safeDrawingPadding()
    ) {
        Text(
            text = "Discovering the Bitmovin Stream Player Component",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(16.dp)
        )

        Text(
            text = "This demonstration showcases the capabilities of the Bitmovin Stream Player component in an Android app.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )

        // Draw image from drawable
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.carbon),
                contentDescription = "Bitmovin Logo",
                modifier = Modifier
                    .fillMaxWidth()
            )
        }


        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Video Player Demonstration",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(16.dp)
        )

        BitmovinStream(
            streamId = TestStreamsIds.SINTEL,
            modifier = Modifier
                .aspectRatio(2.35f),
            enableAds = false

        )


        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "The Bitmovin Stream Player component is highly customizable and supports various features such as autoplay, subtitles, and immersive full screen.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(50.dp))
    }
}

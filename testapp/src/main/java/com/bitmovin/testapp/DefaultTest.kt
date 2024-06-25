package com.bitmovin.testapp

import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bitmovin.streams.BitmovinStream
import com.bitmovin.streams.TestStreamsIds
import com.bitmovin.streams.config.BitmovinStreamConfig

class DefaultTest : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val scrollState = rememberScrollState()
            var selectedConfig by remember { mutableStateOf(TESTCASES_ERRORS.keys.first()) }

            Column(Modifier.safeDrawingPadding()) {
                BitmovinStream(config = TESTCASES_ERRORS[selectedConfig]!!)
                Text(text = "Error messages Tests", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(4.dp))
                Row(
                    Modifier
                        .horizontalScroll(scrollState)) {
                    TESTCASES_ERRORS.keys.forEach { name ->
                        Button(
                            onClick = { selectedConfig = name },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(text = name)
                        }
                    }
                }
            }
        }
    }
}

val TESTCASES_ERRORS = mapOf(
    "404 (Stream not found)" to BitmovinStreamConfig(streamId = TestStreamsIds.SQUARE_VIDEO+"d"),
    "401 (Unauthorized)" to BitmovinStreamConfig(streamId = TestStreamsIds.BIG_BUCK_BUNNY),
    "403 (Forbidden)" to BitmovinStreamConfig(streamId = TestStreamsIds.BIG_BUCK_BUNNY, jwToken = "nonvalid"),
    // Add more test cases as needed
)


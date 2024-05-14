package com.bitmovin.streams

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bitmovin.streams.streamsjson.StreamConfigData

const val MAX_FETCH_ATTEMPTS_STREAMS_CONFIG = 3

/**
 * Bitmovin Streams Player Component.
 *
 * @param streamId The id of the stream to be played.
 *
 * @param jwToken The token to be used for authentication.
 * @param autoPlay Whether the player should start playing automatically.
 * @param muted Whether the player should be muted.
 * @param poster The poster image to be displayed before the player starts.
 * @param start The time in seconds at which the player should start playing.
 */
@Composable
fun StreamsPlayer(
    streamId: String,
    jwToken : String? = null,
    autoPlay : Boolean = false,
    muted : Boolean = false,
    poster : String? = null,
    start : Double = 0.0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var streamConfigData by remember { mutableStateOf<StreamConfigData?>(null) }
    var streamResponseError by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf(StreamDataBridgeState.FETCHING) }

    when (state) {
        StreamDataBridgeState.FETCHING -> {

            // Fetch the stream config data
            LaunchedEffect(key1 = streamId, key2 = jwToken) {
                var tryCount = 0
                while (true) {
                    // Fetch the stream config data
                    val streamConfigDataResp = getStreamConfigData(streamId, jwToken)
                    streamResponseError = streamConfigDataResp.responseHttpCode
                    if (streamConfigDataResp.responseHttpCode != 200) {
                        if (tryCount < MAX_FETCH_ATTEMPTS_STREAMS_CONFIG) {
                            tryCount++
                            continue
                        }
                        // Happens only if the maximum number of attempts is reached
                        state = StreamDataBridgeState.DISPLAYING_ERROR
                        break
                    }

                    streamConfigData = streamConfigDataResp.streamConfigData

                    state = StreamDataBridgeState.DISPLAYING
                    break
                }
            }
        }
        StreamDataBridgeState.DISPLAYING -> {
            if (streamConfigData != null) {
                // Display the player
                TextVideoPlayerFiller("Stream Player of id : ${streamConfigData!!.analytics.videoId}", modifier = modifier)
            } else {
                Log.e("StreamsPlayer", "StreamConfigData is null | SHOULD NOT HAPPEN HERE!")
                state = StreamDataBridgeState.DISPLAYING_ERROR
            }
        }
        StreamDataBridgeState.DISPLAYING_ERROR -> {
            // Temporary error message
            TextVideoPlayerFiller("Error: $streamResponseError", modifier = modifier)
        }
    }
}

/**
 * Video player replacement element for handling errors and waiting time.
 * @param text The text to be displayed.
 */
@Composable
fun TextVideoPlayerFiller(text : String, modifier: Modifier = Modifier) {
    Text(text =  "Not implemented yet: msg : $text", modifier = modifier)
}

enum class StreamDataBridgeState {
    FETCHING,
    DISPLAYING,
    DISPLAYING_ERROR,
}


package com.bitmovin.streams

import android.util.Log
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bitmovin.player.PlayerView
import com.bitmovin.player.SubtitleView
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
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
    modifier: Modifier = Modifier,
    jwToken : String? = null,
    autoPlay : Boolean = false,
    muted : Boolean = false,
    poster : String? = null,
    start : Double = 0.0,
    subtitles : List<SubtitleTrack> = emptyList(),
) {
    Log.d("StreamsPlayer", "StreamsPlayer called")
    val context = LocalContext.current

    val isFullScreen = remember { mutableStateOf(false) }
    val fullScreenHandler = FullScreenHandler(isFullScreen)
    var streamConfigData by remember { mutableStateOf<StreamConfigData?>(null) }
    var streamResponseError by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf(StreamDataBridgeState.FETCHING) }

    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    var subtitlesView by remember { mutableStateOf<SubtitleView?>(null) }

    when (state) {
        StreamDataBridgeState.FETCHING -> {

            // Fetch the stream config data
            LaunchedEffect(key1 = streamId, key2 = jwToken) {

                Log.d("StreamsPlayer", "Fetching stream config data for stream $streamId")
                // Fetch the stream config data
                val streamConfigDataResp = getStreamConfigData(streamId, jwToken)
                streamResponseError = streamConfigDataResp.responseHttpCode
                when (streamResponseError) {
                    200 -> {
                        streamConfigData = streamConfigDataResp.streamConfigData
                        state = StreamDataBridgeState.INITILIZING
                    }
                    401 -> {
                        Log.e("StreamsPlayer", "Unauthorized access to stream\nThis stream may be private or require a token.")
                        state = StreamDataBridgeState.DISPLAYING_ERROR
                    }
                    403 -> {
                        Log.e("StreamsPlayer", "Forbidden access to stream\nThe domain may not be allowed to access the stream or the token you provided may be invalid.")
                        state = StreamDataBridgeState.DISPLAYING_ERROR
                    }
                    else -> {
                        Log.e("StreamsPlayer", "Error fetching stream config data.")
                        state = StreamDataBridgeState.DISPLAYING_ERROR
                    }

                }
            }
        }
        StreamDataBridgeState.INITILIZING -> {
            if (streamConfigData == null) {
                Log.e("StreamsPlayer", "StreamConfigData is null | SHOULD NOT HAPPEN HERE!")
                state = StreamDataBridgeState.DISPLAYING_ERROR
            }

            val streamConfig = streamConfigData!!
            val player = createPlayer(streamConfig, context)
            val streamSource = createSource(streamConfig, customPosterSource = poster, subtitlesSources = subtitles)

            // Loading the stream source
            player.load(streamSource)

            // Handling properties
            if (autoPlay)
                player.play()
            if (muted)
                player.mute()

            player.seek(start)

            // UI
            subtitlesView = SubtitleView(context)
            subtitlesView!!.setPlayer(player)

            playerView = createPlayerView(context, player)
            playerView!!.setFullscreenHandler(fullScreenHandler)

            state = StreamDataBridgeState.DISPLAYING
        }
        StreamDataBridgeState.DISPLAYING_ERROR -> {
            when (streamResponseError) {
                401 -> {
                    TextVideoPlayerFiller("Unauthorized access to stream")
                }
                403 -> {
                    TextVideoPlayerFiller("Forbidden access to stream")
                }
                else -> {
                    TextVideoPlayerFiller("Error fetching stream config data")
                }
            }
        }
        StreamDataBridgeState.DISPLAYING -> {

            subtitlesView!!
            if (isFullScreen.value) {
                Dialog(
                    onDismissRequest = { isFullScreen.value = false },
                    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
                ){
                    Column(modifier = modifier) {
                        AndroidView(factory = { playerView!! })
                        AndroidView(factory = { subtitlesView!! })
                    }
                }
            } else {
                Column(modifier = modifier) {
                    AndroidView(factory = { playerView!! })
                    AndroidView(factory = { subtitlesView!! })
                }
            }
        }
    }
}

/**
 * Video player replacement element for handling errors and waiting time.
 * @param text The text to be displayed.
 */
@Composable
fun TextVideoPlayerFiller(text : String, modifier: Modifier = Modifier) {
    Text(text =  "Not implemented yet : $text", modifier = modifier)
}

enum class StreamDataBridgeState {
    FETCHING,
    INITILIZING,
    DISPLAYING,
    DISPLAYING_ERROR,
}


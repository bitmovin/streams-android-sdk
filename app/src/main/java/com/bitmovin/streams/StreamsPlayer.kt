package com.bitmovin.streams

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
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
import androidx.lifecycle.viewmodel.compose.viewModel
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
@OptIn(ExperimentalMaterial3Api::class)
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
    val viewModel: ViewModelStream = viewModel()
    val state = viewModel.state

    when (state) {
        StreamDataBridgeState.FETCHING -> {
            LaunchedEffect(Unit) {
                viewModel.fetchStreamConfigData(streamId, jwToken)
            }
            TextVideoPlayerFiller("Fetching stream data", modifier)
        }
        StreamDataBridgeState.INITIALIZING -> {
            LaunchedEffect(Unit) {
                viewModel.initializePlayer(context, viewModel.streamConfigData!!, autoPlay, muted, start, poster, subtitles)
            }
            TextVideoPlayerFiller("Initializing player", modifier)
        }
        StreamDataBridgeState.DISPLAYING_ERROR -> {
            TextVideoPlayerFiller("Error fetching stream data", modifier)
        }
        StreamDataBridgeState.DISPLAYING -> {
            val playerView = viewModel.playerView!!
            val subtitlesView = viewModel.subtitlesView!!

            if (viewModel.isFullScreen.value) {
                Dialog(
                    onDismissRequest = { viewModel.isFullScreen.value = false },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = false,
                        usePlatformDefaultWidth = false
                    )
                ) {
                    Column(modifier = modifier) {
                        AndroidView(factory = { playerView })
                        AndroidView(factory = { subtitlesView })
                    }
                }
            } else {
                Column(modifier = modifier) {
                    AndroidView(factory = { playerView })
                    AndroidView(factory = { subtitlesView })
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


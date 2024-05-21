package com.bitmovin.streams

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import java.util.UUID

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
    streamId : String,
    modifier : Modifier = Modifier,
    jwToken : String? = null,
    autoPlay : Boolean = false,
    muted : Boolean = false,
    poster : String? = null,
    start : Double = 0.0,
    subtitles : List<SubtitleTrack> = emptyList(),
    immersiveFullScreen : Boolean = true
) {
    Log.d("StreamsPlayer", "StreamsPlayer called")
    val context = LocalContext.current

    // Make the ViewModel unique for each instance of the Streams Player
    val uuid by remember { mutableStateOf(UUID.randomUUID().toString()) }
    val viewModel: ViewModelStream = viewModel(key = uuid)
    val state = viewModel.state

    when (state) {
        StreamDataBridgeState.DISPLAYING -> {
            val playerView = viewModel.playerView!!
            val subtitlesView = viewModel.subtitlesView!!

            if (viewModel.isFullScreen.value) {
                if (immersiveFullScreen)
                    ImmersiveFullScreen(
                        onDismissRequest = { viewModel.isFullScreen.value = false }
                    ) {
                        StreamVideoPlayer(playerView = playerView, subtitleView = subtitlesView)
                    }
                else
                    FullScreen(
                        onDismissRequest = { viewModel.isFullScreen.value = false }
                    ) {
                        StreamVideoPlayer(playerView = playerView, subtitleView = subtitlesView, modifier = Modifier.fillMaxSize())
                    }
            } else {
                StreamVideoPlayer(playerView = playerView, subtitleView = subtitlesView, modifier)
            }
        }

        // One-time actions for fetching and initializing the player
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
            ErrorHandling(error = viewModel.streamResponseError, modifier)
        }
    }
}
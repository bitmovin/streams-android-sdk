package com.bitmovin.streams

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bitmovin.player.api.media.subtitle.SubtitleTrack

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
    val viewModel: ViewModelStream = viewModel()
    val state = viewModel.state

    when (state) {
        StreamDataBridgeState.DISPLAYING -> {
            val playerView = viewModel.playerView!!
            val subtitlesView = viewModel.subtitlesView!!

            // Remove the views from their parent
            // This is necessary to avoid the current child to be
            // added to the parent again while leaving full screen mode
            playerView.removeFromParent()
            subtitlesView.removeFromParent()

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
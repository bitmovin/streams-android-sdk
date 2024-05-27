package com.bitmovin.streams

import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import java.util.UUID


const val MAX_FETCH_ATTEMPTS_STREAMS_CONFIG = 3

/**
 * Bitmovin Streams Player Component.
 *
 * @param streamId The id of the stream to be played.
 *
 * @param modifier The modifier to be applied to the player.
 * @param jwToken The token to be used for authentication if the stream is protected.
 * @param autoPlay Whether the player should start playing automatically.
 * @param muted Whether the player should be muted.
 * @param poster The poster image to be displayed before the player starts.
 * @param start The time in seconds at which the player should start playing.
 * @param subtitles The list of subtitle tracks available for the stream.
 * @param immersiveFullScreen Whether the player should be in immersive full screen mode. Should only be true if edgeToEdge is enabled.
 */
@Composable
fun BitmovinStream(
    streamId : String,
    modifier : Modifier = Modifier,
    jwToken : String? = null,
    autoPlay : Boolean = false,
    muted : Boolean = false,
    poster : String? = null,
    start : Double = 0.0,
    subtitles : List<SubtitleTrack> = emptyList(),
    immersiveFullScreen : Boolean = false,
    bitmovinStreamEventListener: BitmovinStreamEventListener? = null,
    screenOrientationOnFullscreenEscape: Int? = null,
    enableAds : Boolean = true

) {
    Log.d("StreamsPlayer", "StreamsPlayer called")
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // The UPID (Unique Player ID) is maintained through recompositions to keep the ViewModel alive and used.
    // We do not use the streamId to allow to user to have multiple players with the same streamId.
    val upid: String by rememberSaveable { UUID.randomUUID().toString() }
    // Make the StreamViewModel unique for each instance of the Streams Player (1:1 relationship)
    val viewModel: ViewModelStream = viewModel(key = upid)
    // PiP related stuffs
    PictureInPictureHandlerForStreams(viewModel)
    when (viewModel.state) {
        BitmovinStreamState.DISPLAYING -> {
            // Should be safe to unwrap as we are in the DISPLAYING state and the playerView should NEVER be null at this point
            val playerView = viewModel.playerView!!
            val subtitlesView = viewModel.subtitlesView!!

            key (playerView.isFullscreen, playerView.isPictureInPicture) {
                if (playerView.isFullscreen) {
                    FullScreen(
                        onDismissRequest = { viewModel.playerView?.exitFullscreen() },
                        immersive = viewModel.immersiveFullScreen.value
                    ) {
                        StreamVideoPlayer(playerView = playerView, subtitleView = subtitlesView)
                    }
                } else {
                    StreamVideoPlayer(
                        playerView = playerView,
                        subtitleView = subtitlesView,
                        modifier = modifier
                    )
                }
            }
        }


        // One-time actions for fetching and initializing the player
        BitmovinStreamState.FETCHING -> {
            LaunchedEffect(Unit) {
                viewModel.fetchStreamConfigData(streamId, jwToken)
            }
            TextVideoPlayerFiller("Fetching stream data", modifier)
        }
        BitmovinStreamState.INITIALIZING -> {
            LaunchedEffect(Unit) {
                viewModel.initializePlayer(context, lifecycleOwner = lifecycleOwner, streamEventListener = bitmovinStreamEventListener, viewModel.streamConfigData!!, autoPlay, muted, start, poster, subtitles, immersiveFullScreen, screenOrientationOnFullscreenEscape, enableAds)
            }
            TextVideoPlayerFiller("Initializing player", modifier)
        }
        BitmovinStreamState.DISPLAYING_ERROR -> {
            ErrorHandling(error = viewModel.streamResponseError, modifier)
        }
    }
}

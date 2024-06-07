package com.bitmovin.streams

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.streams.config.BitmovinStreamConfig
import com.bitmovin.streams.config.BitmovinStreamEventListener
import com.bitmovin.streams.config.FullscreenConfig
import com.bitmovin.streams.config.StyleConfigStream
import java.util.UUID


private const val MAX_FETCH_ATTEMPTS_STREAMS_CONFIG = 3


@Composable
fun BitmovinStream(
    config: BitmovinStreamConfig
) {
BitmovinStream(
        streamId = config.streamId,
        modifier = config.modifier,
        jwToken = config.jwToken,
        autoPlay = config.autoPlay,
        muted = config.muted,
        poster = config.poster,
        start = config.start,
        subtitles = config.subtitles,
        fullscreenConfig = config.fullscreenConfig,
        bitmovinStreamEventListener = config.streamEventListener,
        enableAds = config.enableAds,
        styleConfig = config.styleConfig
    )
}

/**
 * Bitmovin Streams Player Component.
 *
 * @param streamId The id of the stream to be played.
 *
 * @param modifier The modifier to be applied to the player.
 * @param jwToken The token to be used for authentication if the stream is protected.
 * @param autoPlay Whether the player should start playing automatically.
 * @param muted Whether the player should be muted.
 * @param poster The poster image to be displayed before the player starts. This property has priority over the poster image from the dashboard.
 * @param start The time in seconds at which the player should start playing.
 * @param subtitles The list of subtitle tracks available for the stream.
 * @param fullscreenConfig The configuration for the fullscreen mode.
 * @param bitmovinStreamEventListener The listener for the player events.
 * @param enableAds Whether ads should be enabled.
 * @param styleConfig The style configuration for the player. This property has priority over the style configuration from the dashboard.
 */
@Composable
fun BitmovinStream(
    streamId : String,
    modifier : Modifier = Modifier,
    jwToken : String? = null,
    autoPlay : Boolean = false,
    muted : Boolean = true, // temporary to avoid loud noises in the office when I forget to turn off the sound
    poster : String? = null,
    start : Double = 0.0,
    subtitles : List<SubtitleTrack> = emptyList(),
    fullscreenConfig: FullscreenConfig = FullscreenConfig(),
    bitmovinStreamEventListener: BitmovinStreamEventListener? = null,
    enableAds : Boolean = true,
    styleConfig : StyleConfigStream = StyleConfigStream()
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

            key (playerView.isFullscreen, playerView.isPictureInPicture) {
                if (playerView.isFullscreen) {
                    FullScreen(
                        onDismissRequest = { viewModel.playerView?.exitFullscreen() },
                        isImmersive = viewModel.immersiveFullScreen.value
                    ) {
                        StreamVideoPlayer(playerView = playerView)
                    }
                } else {
                    StreamVideoPlayer(
                        playerView = playerView,
                        modifier = modifier
                    )
                }
            }
        }


        // One-time actions for fetching and initializing the player
        BitmovinStreamState.FETCHING,
        BitmovinStreamState.INITIALIZING,
        BitmovinStreamState.WAITING_FOR_VIEW,
        BitmovinStreamState.WAITING_FOR_PLAYER -> {
            var loadingMess: String
            if (BitmovinStreamState.FETCHING == viewModel.state) {
                LaunchedEffect(Unit) {
                    viewModel.fetchStreamConfigData(streamId, jwToken)
                }
                loadingMess = "Fetching stream config data"
            } else if (BitmovinStreamState.INITIALIZING == viewModel.state) {
                LaunchedEffect(Unit) {
                    viewModel.initializePlayer(context, streamId, lifecycleOwner = lifecycleOwner, streamEventListener = bitmovinStreamEventListener, viewModel.streamConfigData!!, autoPlay, muted, start, poster, subtitles, fullscreenConfig, enableAds, styleConfig)
                }
                loadingMess = "Initializing player"
            } else {
                loadingMess = if (BitmovinStreamState.WAITING_FOR_VIEW == viewModel.state) {
                    "Waiting for player view"
                } else {
                    "Waiting for player"
                }
            }
            TextVideoPlayerFiller(loadingMess, modifier, loadingEffect = true)
        }
        BitmovinStreamState.DISPLAYING_ERROR -> {
            ErrorHandling(error = viewModel.streamResponseError, modifier)
        }
    }
}

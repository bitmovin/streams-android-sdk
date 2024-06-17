package com.bitmovin.streams

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.streams.config.BitmovinStreamConfig
import com.bitmovin.streams.config.BitmovinStreamEventListener
import com.bitmovin.streams.config.FullscreenConfig
import com.bitmovin.streams.config.StyleConfigStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        streamEventListener = config.streamEventListener,
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
 * @param loop Whether the player should loop the stream.
 * @param muted Whether the player should be muted.
 * @param poster The poster image to be displayed before the player starts. This property has priority over the poster image from the dashboard.
 * @param start The time in seconds at which the player should start playing.
 * @param subtitles The list of subtitle tracks available for the stream.
 * @param fullscreenConfig The configuration for the fullscreen mode.
 * @param streamEventListener The listener for the player events.
 * @param enableAds Whether ads should be enabled.
 * @param styleConfig The style configuration for the player. This property has priority over the style configuration from the dashboard.
 */
@Composable
fun BitmovinStream(
    streamId : String,
    modifier : Modifier = Modifier,
    jwToken : String? = null,
    autoPlay : Boolean = false,
    loop : Boolean = false,
    muted : Boolean = true, // temporary true to avoid loud noises in the office when I forget to turn off the sound
    poster : String? = null,
    start : Double = 0.0,
    subtitles : List<SubtitleTrack> = emptyList(),
    fullscreenConfig: FullscreenConfig = FullscreenConfig(),
    streamEventListener: BitmovinStreamEventListener? = null,
    enableAds : Boolean = true,
    styleConfig : StyleConfigStream = StyleConfigStream()
) {
    val recompositionTimeStart = System.currentTimeMillis()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // The UPID (Unique Player ID) is maintained through recompositions to keep the ViewModel alive and used.
    // We do not use the streamId to allow to user to have multiple players with the same streamId.
    val upid: String by rememberSaveable { UUID.randomUUID().toString() }
    // Make the StreamViewModel unique for each instance of the Streams Player (1:1 relationship)
    val stream: Stream = StreamsProvider.getInstance().getStream(upid)

    when (stream.state) {
        BitmovinStreamState.DISPLAYING -> {
            // Safe to unwrap as we are in the DISPLAYING state and the playerView should NEVER be null at this point
            val playerView = stream.playerView!!

            if (playerView.isFullscreen) {
                FullScreen(
                    onDismissRequest = { stream.playerView?.exitFullscreen() },
                    isImmersive = fullscreenConfig.immersive
                ) {
                    StreamVideoPlayer(playerView = playerView)
                }
                TextVideoPlayerFiller(text = "In Fullscreen", modifier)
            } else {
                StreamVideoPlayer(
                    playerView = playerView,
                    modifier = modifier
                )
            }
        }


        // One-time actions for fetching and initializing the player
        BitmovinStreamState.FETCHING,
        BitmovinStreamState.INITIALIZING,
        BitmovinStreamState.WAITING_FOR_VIEW,
        BitmovinStreamState.WAITING_FOR_PLAYER -> {
            if (BitmovinStreamState.FETCHING == stream.state) {
                LaunchedEffect(Unit) {
                    stream.fetchStreamConfig(streamId, jwToken, streamEventListener)
                }
            } else if (BitmovinStreamState.INITIALIZING == stream.state) {
                LaunchedEffect(Unit) {
                    stream.initializeStream(context, lifecycleOwner = lifecycleOwner, stream.streamConfigData!!, autoPlay, loop, muted, start, poster, subtitles, fullscreenConfig, enableAds, styleConfig)
                }
            }
            val loadingMess: String = getLoadingScreenMessage(stream.state)
            TextVideoPlayerFiller(loadingMess, modifier, loadingEffect = true)
        }
        BitmovinStreamState.DISPLAYING_ERROR -> {
            ErrorHandling(error = stream.streamResponseError, modifier)
        }
    }
    Log.i(Tag.Stream, "[$upid] Stream recomposed in ${System.currentTimeMillis() - recompositionTimeStart}ms")

    DisposableEffect(Unit) {

        onDispose {
            stream.dispose()
        }
    }
}

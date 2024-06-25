package com.bitmovin.streams

import android.util.Log
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.streams.config.BitmovinStreamConfig
import com.bitmovin.streams.config.BitmovinStreamEventListener
import com.bitmovin.streams.config.FullscreenConfig
import com.bitmovin.streams.config.StyleConfigStream
import java.util.UUID

/**
 * Bitmovin Streams Player Component.
 *
 * @param config The configuration for the player.
 * @param modifier The modifier to be applied to the stream player.
 */
@Composable
fun BitmovinStream(
    config: BitmovinStreamConfig,
    modifier: Modifier = Modifier.aspectRatio(16f / 9f)
) {
    BitmovinStream(
        streamId = config.streamId,
        modifier = modifier,
        jwToken = config.jwToken,
        autoPlay = config.autoPlay,
        loop = config.loop,
        muted = config.muted,
        poster = config.poster,
        start = config.start,
        subtitles = config.subtitles,
        fullscreenConfig = config.fullscreenConfig,
        streamEventListener = config.streamEventListener,
        enableAds = config.enableAds,
        styleConfig = config.styleConfig,
        allLogs = config.allLogs
    )
}

/**
 * Bitmovin Streams Player Component.
 *
 * @param streamId The id of the stream to be played.
 *
 * @param modifier The modifier to be applied to the stream player.
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
    streamId: String,
    modifier: Modifier = Modifier.aspectRatio(16f / 9f),
    jwToken: String? = null,
    autoPlay: Boolean = false,
    loop: Boolean = false,
    muted: Boolean = true, // temporary true to avoid loud noises in the office when I forget to turn off the sound
    poster: String? = null,
    start: Double = 0.0,
    subtitles: List<SubtitleTrack> = emptyList(),
    fullscreenConfig: FullscreenConfig = FullscreenConfig(),
    streamEventListener: BitmovinStreamEventListener? = null,
    enableAds: Boolean = true,
    styleConfig: StyleConfigStream = StyleConfigStream(),
    allLogs: Boolean = true
) {
    val recompositionTimeStart = System.currentTimeMillis()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // The USID (Unique Stream ID) is maintained through recompositions to keep the link between the component and the stream.
    // Warning : The USID is not the same as the streamId. The streamId is the id of the stream to be played.
    // We do not use the streamId to allow to user to have multiple players with the same streamId.
    val usid: String by rememberSaveable { UUID.randomUUID().toString() }
    // Make the Stream unique for each instance of the Streams Player (1:1 relationship)
    val stream: Stream = StreamsProvider.getInstance().getStream(usid, allLogs)

    when (stream.state) {
        BitmovinStreamState.DISPLAYING -> {
            // Safe to unwrap as we are in the DISPLAYING state and the playerView should NEVER be null at this point
            val playerView = stream.playerView!!

            if (playerView.isFullscreen) {
                FullScreen(
                    onDismissRequest = { stream.playerView?.exitFullscreen() },
                    isImmersive = fullscreenConfig.immersive
                ) { StreamVideoPlayer(playerView = playerView) }
                TextVideoPlayerFiller(text = "In Fullscreen", modifier)
            } else {
                StreamVideoPlayer(
                    playerView = playerView,
                    modifier = modifier
                )
            }
        }

        BitmovinStreamState.DISPLAYING_ERROR -> {
            ErrorHandling(streamError = stream.streamError, modifier)
        }

        else -> {
            TextVideoPlayerFiller(
                getLoadingScreenMessage(stream.state),
                modifier,
                loadingEffect = true
            )
        }
    }
    stream.logger.performance(
        "Stream recomposition",
        System.currentTimeMillis() - recompositionTimeStart,
    )

        DisposableEffect(Unit) {
            stream.initStream(
                context = context,
                lifecycleOwner = lifecycleOwner,
                streamId = streamId,
                jwToken = jwToken,
                autoPlay = autoPlay,
                loop = loop,
                muted = muted,
                poster = poster,
                start = start,
                subtitles = subtitles,
                fullscreenConfig = fullscreenConfig,
                bitmovinStreamEventListener = streamEventListener,
                enableAds = enableAds,
                styleConfigStream = styleConfig
            )
            onDispose {
                stream.dispose()
            }
        }
}

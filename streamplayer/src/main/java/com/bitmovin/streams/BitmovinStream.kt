package com.bitmovin.streams

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.streams.config.StreamConfig
import com.bitmovin.streams.config.StreamListener
import com.bitmovin.streams.config.FullscreenConfig
import com.bitmovin.streams.config.StyleConfigStream
import java.util.UUID

/**
 * Bitmovin Streams Player Component.
 */
@Composable
fun BitmovinStream(
    /**
     * The configuration for the player.
     */
    config: StreamConfig,

    /**
     * The modifier to be applied to the stream player.
     */
    modifier: Modifier = Modifier
) {
    BitmovinStream(
        streamId = config.streamId,
        modifier = modifier,
        authenticationToken = config.authenticationToken,
        autoPlay = config.autoPlay,
        loop = config.loop,
        muted = config.muted,
        poster = config.poster,
        startTime = config.startTime,
        subtitles = config.subtitles,
        fullscreenConfig = config.fullscreenConfig,
        streamListener = config.streamListener,
        enableAds = config.enableAds,
        styleConfig = config.styleConfig,
        allLogs = config.allLogs
    )
}

/**
 * Bitmovin Streams Player Component.
 */
@Composable
fun BitmovinStream(
    /**
     * The streamId of the stream to be played.
     */
    streamId: String,

    /**
     * The modifier to be applied to the stream player.
     */
    modifier: Modifier = Modifier,

    /**
     * The token to be used for authentication if the stream is protected.
     * @see <a href="https://developer.bitmovin.com/streams/docs/secure-your-streams-with-signed-urls">Secure your Streams</a>
     */
    authenticationToken: String? = null,

    /**
     * Whether the player should start playing automatically.
     */
    autoPlay: Boolean = false,

    /**
     * Whether the player should loop the stream.
     */
    loop: Boolean = false,

    /**
     * Whether the player should be muted.
     */
    muted: Boolean = false,

    /**
     * The poster image to be displayed before the player starts. This property has priority over the poster image from the dashboard.
     */
    poster: String? = null,

    /**
     * The time in seconds at which the player should start playing.
     */
    startTime: Double = 0.0,

    /**
     * The list of subtitle tracks available for the stream.
     */
    subtitles: List<SubtitleTrack> = emptyList(),

    /**
     * The configuration for the fullscreen mode.
     */
    fullscreenConfig: FullscreenConfig = FullscreenConfig(),

    /**
     * The listener for the player events.
     */
    streamListener: StreamListener? = null,

    /**
     * Whether ads should be enabled.
     */
    enableAds: Boolean = true,

    /**
     * The style configuration for the player. This property has priority over the style configuration from the dashboard.
     */
    styleConfig: StyleConfigStream = StyleConfigStream(),

    /**
     * Whether all logs should be displayed.
     */
    allLogs: Boolean = false
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

    // Launched on Composition
    LaunchedEffect(Unit) {
        stream.initStream(
            context = context,
            lifecycleOwner = lifecycleOwner,
            streamId = streamId,
            jwToken = authenticationToken,
            autoPlay = autoPlay,
            loop = loop,
            muted = muted,
            poster = poster,
            start = startTime,
            subtitles = subtitles,
            fullscreenConfig = fullscreenConfig,
            streamListener = streamListener,
            enableAds = enableAds,
            styleConfigStream = styleConfig
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            stream.dispose()
        }
    }
}

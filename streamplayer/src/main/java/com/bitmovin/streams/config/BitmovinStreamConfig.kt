package com.bitmovin.streams.config

import androidx.compose.ui.Modifier
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.streams.StreamError

/**
 * Configuration for the BitmovinStream
 *
 * @property streamId The streamId of the stream to be played.
 * @property jwToken The token to be used for authentication if the stream is protected.
 * @property autoPlay Whether the player should start playing automatically.
 * @property muted Whether the player should be muted.
 * @property poster The poster image to be displayed before the player starts. This property has priority over the poster image from the dashboard.
 * @property start The time in seconds at which the player should start playing.
 * @property loop Whether the player should loop the stream.
 * @property fullscreenConfig The configuration for the fullscreen mode.
 * @property subtitles The list of subtitle tracks available for the stream.
 * @property streamEventListener The listener for the player events.
 * @property enableAds Whether ads should be enabled.
 * @property styleConfig The style configuration for the player. This property has priority over the style configuration from the dashboard.
 */

data class BitmovinStreamConfig(
    var streamId: String,
    var jwToken: String? = null,
    var autoPlay: Boolean = false,
    var muted: Boolean = false,
    var poster: String? = null,
    var start: Double = 0.0,
    var loop: Boolean = false,
    var fullscreenConfig: FullscreenConfig,
    var subtitles: List<SubtitleTrack> = emptyList(),
    var streamEventListener: BitmovinStreamEventListener? = null,
    var enableAds: Boolean = true,
    var styleConfig: StyleConfigStream = StyleConfigStream()
) {

    /**
     * Configuration for the BitmovinStream
     *
     * @param streamId The streamId of the stream to be played.
     * @param jwToken The token to be used for authentication if the stream is protected.
     * @param autoPlay Whether the player should start playing automatically.
     * @param muted Whether the player should be muted.
     * @param poster The poster image to be displayed before the player starts. This property has priority over the poster image from the dashboard.
     * @param start The time in seconds at which the player should start playing.
     * @param loop Whether the player should loop the stream.
     * @param fullscreenConfig The configuration for the fullscreen mode.
     * @param subtitles The list of subtitle tracks available for the stream.
     * @param styleConfig The style configuration for the player. This property has priority over the style configuration from the dashboard.
     * @param onStreamReady Called when the stream is ready to be played.
     * @param onStreamError Called when an error occurs during the stream setup.
     * @param enableAds Whether ads should be enabled.
     * @param styleConfig The style configuration for the player. This property has priority over the style configuration from the dashboard.
     */
    constructor(
        streamId: String,
        jwToken: String? = null,
        autoPlay: Boolean = false,
        muted: Boolean = false,
        poster: String? = null,
        start: Double = 0.0,
        loop: Boolean = false,
        fullscreenConfig: FullscreenConfig = FullscreenConfig(),
        subtitles: List<SubtitleTrack> = emptyList(),
        onStreamReady: (Player, PlayerView) -> Unit = { _, _ -> },
        onStreamError: (StreamError) -> Unit = { _ -> },
        enableAds: Boolean = true,
        styleConfig: StyleConfigStream = StyleConfigStream()
    ) : this(
        streamId = streamId,
        jwToken = jwToken,
        autoPlay = autoPlay,
        muted = muted,
        poster = poster,
        start = start,
        loop = loop,
        fullscreenConfig = fullscreenConfig,
        subtitles = subtitles,
        streamEventListener = object :
            BitmovinStreamEventListener {
            override fun onStreamReady(player: Player, playerView: PlayerView) {
                onStreamReady(player, playerView)
            }

            override fun onStreamError(error: StreamError) {
                onStreamError(error)
            }
        },
        enableAds = enableAds,
        styleConfig = styleConfig
    )
}

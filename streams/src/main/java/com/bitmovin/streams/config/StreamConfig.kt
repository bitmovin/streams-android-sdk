package com.bitmovin.streams.config

import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.media.subtitle.SubtitleTrack

/**
 * Configuration for the BitmovinStream.
 *
 * @see com.bitmovin.streams.BitmovinStream
 */

public class StreamConfig(
    /**
     * The streamId of the stream to be played.
     */
    public var streamId: String,
    /**
     * The token to be used for authentication if the stream is protected.
     */
    public var authenticationToken: String? = null,
    /**
     * Whether the player should start playing automatically.
     */
    public var autoPlay: Boolean = false,
    /**
     * Whether the player should be muted.
     */
    public var muted: Boolean = false,
    /**
     * The poster image to be displayed before the player starts. This property has priority over the poster image from the dashboard.
     */
    public var poster: String? = null,
    /**
     * The time in seconds at which the player should start playing.
     */
    public var startTime: Double = 0.0,
    /**
     * Whether the player should loop the stream.
     */
    public var loop: Boolean = false,
    /**
     * The configuration for the fullscreen mode.
     */
    public var fullscreenConfig: FullscreenConfig,
    /**
     * The list of subtitle tracks available for the stream.
     */
    public var subtitles: List<SubtitleTrack> = emptyList(),
    /**
     * The listener for the player events.
     */
    public var streamListener: StreamListener? = null,
    /**
     * Whether ads should be enabled.
     */
    public var enableAds: Boolean = true,
    /**
     * The style configuration for the player. This property has priority over the style configuration from the dashboard.
     */
    public var styleConfig: StyleConfigStream = StyleConfigStream(),
    /**
     * Whether all logs should be displayed.
     */
    public var allLogs: Boolean = false,
) {
    /**
     * Configuration for the BitmovinStream
     */
    public constructor(
        /**
         * The streamId of the stream to be played.
         */
        streamId: String,
        /**
         * The token to be used for authentication if the stream is protected.
         * @see <a href="https://developer.bitmovin.com/streams/docs/secure-your-streams-with-signed-urls">Bitmovin Player UI CSS Class Reference</a>
         */
        authenticationToken: String? = null,
        /**
         * Whether the player should start playing automatically.
         */
        autoPlay: Boolean = false,
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
         * Whether the player should loop the stream.
         */
        loop: Boolean = false,
        /**
         * The configuration for the fullscreen mode.
         */
        fullscreenConfig: FullscreenConfig = FullscreenConfig(),
        /**
         * The list of subtitle tracks available for the stream.
         */
        subtitles: List<SubtitleTrack> = emptyList(),
        /**
         * Called when the player is ready to play the stream.
         */
        onStreamReady: (Player, PlayerView) -> Unit = { _, _ -> },
        /**
         * Called when an error occurs while loading the stream.
         */
        onStreamError: (StreamError) -> Unit = { _ -> },
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
        allLogs: Boolean = false,
    ) : this(
        streamId = streamId,
        authenticationToken = authenticationToken,
        autoPlay = autoPlay,
        muted = muted,
        poster = poster,
        startTime = startTime,
        loop = loop,
        fullscreenConfig = fullscreenConfig,
        subtitles = subtitles,
        streamListener =
            object :
                StreamListener {
                override fun onStreamReady(
                    player: Player,
                    playerView: PlayerView,
                ) {
                    onStreamReady(player, playerView)
                }

                override fun onStreamError(error: StreamError) {
                    onStreamError(error)
                }
            },
        enableAds = enableAds,
        styleConfig = styleConfig,
        allLogs = allLogs,
    )
}

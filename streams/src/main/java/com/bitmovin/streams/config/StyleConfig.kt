package com.bitmovin.streams.config

import androidx.compose.ui.graphics.Color

/**
 * Configuration for the style of the player.
 *
 * @see <a href="https://developer.bitmovin.com/playback/docs/player-ui-css-class-reference">Bitmovin Player UI CSS Class Reference</a>
*/
public class StyleConfigStream(
    /**
     * The color of the playback marker background.
     */
    public var playbackMarkerBgColor: Color? = null,
    /**
     * The color of the playback marker border.
     */
    public var playbackMarkerBorderColor: Color? = null,
    /**
     * The color of the playback track played.
     */
    public var playbackTrackPlayedColor: Color? = null,
    /**
     * The color of the playback track buffered.
     */
    public var playbackTrackBufferedColor: Color? = null,
    /**
     * The color of the playback track background.
     */
    public var playbackTrackBgColor: Color? = null,
    /**
     * The color of the text.
     */
    public var textColor: Color? = null,
    /**
     * The color of the background.
     */
    public var backgroundColor: Color? = null,
    /**
     * CSS rules that you can add to make the player look as you expect. Does not support URL or URI, has to be plain text.
     */
    public var customCss: String = "",
) {
    public companion object {
        /**
         * The default theme of the player.
         */
        public val BITMOVIN_DEFAULT_THEME: StyleConfigStream =
            StyleConfigStream(
                playbackMarkerBgColor = getColor(32, 172, 227, 0.5f),
                playbackMarkerBorderColor = getColor(32, 172, 227, 1f),
                playbackTrackPlayedColor = getColor(32, 172, 227, 1f),
                playbackTrackBufferedColor = getColor(255, 255, 255, 1f),
                playbackTrackBgColor = getColor(255, 255, 255, 0.35f),
                textColor = getColor(255, 255, 255, 1f),
                backgroundColor = getColor(0, 0, 0, 1f),
            )
    }
}

internal fun getColor(
    red: Int,
    green: Int,
    blue: Int,
    alpha: Float,
): Color {
    return Color(red, green, blue, (alpha * 255).toInt())
}

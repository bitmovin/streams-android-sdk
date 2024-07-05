package com.bitmovin.streams.config

import androidx.compose.ui.graphics.Color

/**
 * Configuration for the style of the player.
*/
public data class StyleConfigStream(
    /**
     * The color of the playback marker background.
     */
    var playbackMarkerBgColor: Color? = null,
    /**
     * The color of the playback marker border.
     */
    var playbackMarkerBorderColor: Color? = null,
    /**
     * The color of the playback track played.
     */
    var playbackTrackPlayedColor: Color? = null,
    /**
     * The color of the playback track buffered.
     */
    var playbackTrackBufferedColor: Color? = null,
    /**
     * The color of the playback track background.
     */
    var playbackTrackBgColor: Color? = null,
    /**
     * The color of the text.
     */
    var textColor: Color? = null,
    /**
     * The color of the background.
     */
    var backgroundColor: Color? = null,
    /**
     * CSS rules that you can add to make the player look as you expect. Does not support URL or URI, has to be plain text.
     * @see <a href="https://developer.bitmovin.com/playback/docs/player-ui-css-class-reference">Bitmovin Player UI CSS Class Reference</a>
     */
    var customCss: String = "",
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

        /**
         * An example theme with red colors.
         */
        internal val RED_EXAMPLE_THEME: StyleConfigStream =
            StyleConfigStream(
                playbackMarkerBgColor = getColor(255, 0, 0, 0f),
                playbackMarkerBorderColor = getColor(255, 0, 0, 0f),
                playbackTrackPlayedColor = getColor(245, 7, 7, 1f),
                playbackTrackBufferedColor = getColor(199, 199, 199, 0.8f),
                playbackTrackBgColor = getColor(128, 128, 128, 0.48f),
                textColor = getColor(217, 217, 217, 1f),
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

package com.bitmovin.streams.config

import androidx.compose.ui.graphics.Color

/**
 * This class is used to store the configuration data of a stream.
 * @param playbackMarkerBgColor The color of the playback marker background.
 * @param playbackMarkerBorderColor The color of the playback marker border.
 * @param playbackTrackPlayedColor The color of the playback track played.
 * @param playbackTrackBufferedColor The color of the playback track buffered.
 * @param playbackTrackBgColor The color of the playback track background.
 * @param textColor The color of the text.
 * @param backgroundColor The color of the background.
 * @param customCss CSS rules that you can add to make the player look as you expect. Does not support URL or URI, has to be plain text. Please refer to the the Player UI documentation for more information : https://developer.bitmovin.com/playback/docs/player-ui-css-class-reference.
 */
class StyleConfigStream(
    val playbackMarkerBgColor: Color? = null,
    val playbackMarkerBorderColor: Color? = null,
    val playbackTrackPlayedColor: Color? = null,
    val playbackTrackBufferedColor: Color? = null,
    val playbackTrackBgColor: Color? = null,
    val textColor: Color? = null,
    val backgroundColor: Color? = null,
    val customCss: String = ""
)

class StreamThemes {
    companion object {

        val BITMOVIN_DEFAULT_THEME = StyleConfigStream(
            playbackMarkerBgColor = getColor(32, 172, 227, 0.5f),
            playbackMarkerBorderColor = getColor(32, 172, 227, 1f),
            playbackTrackPlayedColor = getColor(32, 172, 227, 1f),
            playbackTrackBufferedColor = getColor(255, 255, 255, 1f),
            playbackTrackBgColor = getColor(255, 255, 255, 0.35f),
            textColor = getColor(255, 255, 255, 1f),
            backgroundColor = getColor(0, 0, 0, 1f)
        )

        val RED_EXAMPLE_THEME = StyleConfigStream(
            playbackMarkerBgColor = getColor(255, 0, 0, 0f),
            playbackMarkerBorderColor = getColor(255, 0, 0, 0f),
            playbackTrackPlayedColor = getColor(245, 7, 7, 1f),
            playbackTrackBufferedColor = getColor(199, 199, 199, 0.8f),
            playbackTrackBgColor = getColor(128, 128, 128, 0.48f),
            textColor = getColor(217, 217, 217, 1f),
            backgroundColor = getColor(0, 0, 0, 1f)
        )

        val PINK_EXAMPLE_THEME = StyleConfigStream(
            playbackMarkerBgColor = getColor(240, 17, 199, 1f),
            playbackMarkerBorderColor = getColor(255, 102, 229, 1f),
            playbackTrackPlayedColor = getColor(240, 17, 199, 1f),
            playbackTrackBufferedColor = getColor(252, 61, 235, 0.4f),
            playbackTrackBgColor = getColor(166, 0, 123, 0.35f),
            textColor = getColor(253, 125, 255, 1f),
            backgroundColor = getColor(0, 0, 0, 1f)
        )
    }
}

fun getColor(
    red: Int, green: Int, blue: Int, alpha: Float
): Color {
    return Color(red, green, blue, (alpha * 255).toInt())
}
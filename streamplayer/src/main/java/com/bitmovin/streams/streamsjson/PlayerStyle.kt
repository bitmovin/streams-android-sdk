package com.bitmovin.streams.streamsjson

import android.util.Log
import com.bitmovin.streams.config.StyleConfigStream
import com.bitmovin.streams.toCSS

// This class might not be useful if we create a custom endpoint for the css.
class PlayerStyle {
    var playbackMarkerBgColor: String? = null
    var playbackMarkerBorderColor: String? = null
    var playbackTrackPlayedColor: String? = null
    var playbackTrackBufferedColor: String? = null
    var playbackTrackBgColor: String? = null
    var textColor: String? = null
    var backgroundColor: String? = null

    internal fun affectConfig(style : StyleConfigStream) {
        style.playbackMarkerBgColor?.let {
            playbackMarkerBgColor = it.toCSS()
        }
        style.playbackMarkerBorderColor?.let {
            playbackMarkerBorderColor = it.toCSS()
        }
        style.playbackTrackPlayedColor?.let {
            playbackTrackPlayedColor = it.toCSS()
        }
        style.playbackTrackBufferedColor?.let {
            playbackTrackBufferedColor = it.toCSS()
        }
        style.playbackTrackBgColor?.let {
            playbackTrackBgColor = it.toCSS()
        }
        style.textColor?.let {
            textColor = it.toCSS()
        }
        style.backgroundColor?.let {
            Log.d("Color", "Setting background color to ${it.toCSS()}")
            backgroundColor = it.toCSS()
        }
    }
}

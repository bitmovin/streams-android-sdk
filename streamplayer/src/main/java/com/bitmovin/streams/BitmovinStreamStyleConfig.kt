package com.bitmovin.streams

import androidx.compose.ui.graphics.Color

data class StyleConfigStream(
    val playerStyleConfigStream: PlayerStyleConfigStream = PlayerStyleConfigStream(),
    val watermarkUrl : String? = null
)

data class PlayerStyleConfigStream(
    var playbackMarkerBgColor: Color? = null,
    var playbackMarkerBorderColor: Color? = null,
    var playbackTrackPlayedColor: Color? = null,
    var playbackTrackBufferedColor: Color? = null,
    var playbackTrackBgColor: Color? = null,
    var textColor: Color? = null,
    var backgroundColor: Color? = null
)
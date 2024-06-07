package com.bitmovin.streams.config

import androidx.compose.ui.Modifier
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.media.subtitle.SubtitleTrack

data class BitmovinStreamConfig(
    var streamId : String,
    var modifier : Modifier = Modifier,
    var jwToken : String? = null,
    var autoPlay : Boolean = false,
    var muted : Boolean = false,
    var poster : String? = null,
    var start : Double = 0.0,
    var subtitles : List<SubtitleTrack> = emptyList(),
    var immersiveFullScreen : Boolean = true,
    var onPlayerReady : (player: Player) -> Unit = {},
    var onPlayerViewReady : (playerView: PlayerView) -> Unit = {},
    var appDefaultOrientation: Int? = null,
    var enableAds : Boolean = true,
    var styleConfig : StyleConfigStream = StyleConfigStream()
) {
}

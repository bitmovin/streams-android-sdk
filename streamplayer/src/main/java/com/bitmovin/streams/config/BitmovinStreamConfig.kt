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
    var streamEventListener: BitmovinStreamEventListener? = null,
    var appDefaultOrientation: Int? = null,
    var enableAds : Boolean = true,
    var styleConfig : StyleConfigStream = StyleConfigStream()
) {

    constructor(
        streamId : String,
        modifier : Modifier = Modifier,
        jwToken : String? = null,
        autoPlay : Boolean = false,
        muted : Boolean = false,
        poster : String? = null,
        start : Double = 0.0,
        subtitles : List<SubtitleTrack> = emptyList(),
        immersiveFullScreen : Boolean = true,
        onPlayerReady : (Player) -> Unit,
        onPlayerViewReady : (PlayerView) -> Unit,
        appDefaultOrientation: Int? = null,
        enableAds : Boolean = true,
        styleConfig : StyleConfigStream = StyleConfigStream()
    ) : this(
        streamId,
        modifier,
        jwToken,
        autoPlay,
        muted,
        poster,
        start,
        subtitles,
        immersiveFullScreen, object :
            BitmovinStreamEventListener {
                override fun onPlayerReady(player: Player) {
                    onPlayerReady(player)
                }

                override fun onPlayerViewReady(playerView: PlayerView) {
                    onPlayerViewReady(playerView)
                }
            },
        appDefaultOrientation,
        enableAds,
        styleConfig
    )
}

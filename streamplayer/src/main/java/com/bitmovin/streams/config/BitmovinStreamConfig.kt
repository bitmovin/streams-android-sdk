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
    var fullscreenConfig: FullscreenConfig,
    var subtitles : List<SubtitleTrack> = emptyList(),
    var streamEventListener: BitmovinStreamEventListener? = null,
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
        fullscreenConfig: FullscreenConfig = FullscreenConfig(),
        subtitles : List<SubtitleTrack> = emptyList(),
        onStreamReady : (Player, PlayerView) -> Unit = { _, _ -> },
        onPlayerReady : (Player) -> Unit = {},
        onPlayerViewReady : (PlayerView) -> Unit = {},
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
        fullscreenConfig,
        subtitles,
        object :
            BitmovinStreamEventListener {
                override fun onStreamReady(player: Player, playerView: PlayerView) {
                    onStreamReady(player, playerView)
                }
                override fun onPlayerReady(player: Player) {
                    onPlayerReady(player)
                }
                override fun onPlayerViewReady(playerView: PlayerView) {
                    onPlayerViewReady(playerView)
                }
            },
        enableAds,
        styleConfig
    )
}

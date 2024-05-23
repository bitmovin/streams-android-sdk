package com.bitmovin.streams

import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player

interface BitmovinStreamEventListener {
    fun onPlayerReady(player: Player)
    fun onPlayerViewReady(playerView: PlayerView)
}
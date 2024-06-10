package com.bitmovin.streams.config

import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player

interface BitmovinStreamEventListener {
    fun onPlayerReady(player: Player)
    fun onPlayerViewReady(playerView: PlayerView)

    /**
     * Called when the stream is ready to be played.
     */
    fun onStreamReady(player: Player, playerView: PlayerView)

    /**
     * Called when an error occurs during the stream setup.
     */
    fun onStreamError(errorCode: Int, errorMessage: String)
}
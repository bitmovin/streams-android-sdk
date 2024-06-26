package com.bitmovin.streams.config

import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.streams.StreamError

interface StreamListener {
    /**
     * Called when the stream is ready to be played.
     */
    fun onStreamReady(player: Player, playerView: PlayerView)

    /**
     * Called when an error occurs during the stream setup.
     */
    fun onStreamError(error: StreamError)
}
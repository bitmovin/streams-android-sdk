package com.bitmovin.streams.config

import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.streams.StreamError

public interface StreamListener {
    /**
     * Called when the stream is ready to be played.
     */
    public fun onStreamReady(
        player: Player,
        playerView: PlayerView,
    )

    /**
     * Called when an error occurs during the stream setup.
     */
    public fun onStreamError(error: StreamError)
}

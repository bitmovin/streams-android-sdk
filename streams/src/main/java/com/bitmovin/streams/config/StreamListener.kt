package com.bitmovin.streams.config

import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player

/**
 * Listener for the stream events.
 * Implement this interface to receive events from the stream.
 * It is also the gateway to the player and playerView.
 *
 * Note that the Player has it's own events that can be listened to.
 *
 * @see <a href="https://cdn.bitmovin.com/player/android/3/docs/player-core/com.bitmovin.player.api.event/index.html">Player Events Docs</a>
 */
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

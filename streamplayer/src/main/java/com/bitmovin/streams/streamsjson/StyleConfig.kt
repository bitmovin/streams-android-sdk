package com.bitmovin.streams.streamsjson

import com.bitmovin.streams.config.StyleConfigStream


class StyleConfig {
    var playerStyle: PlayerStyle = PlayerStyle()
    var watermarkUrl: String? = null
    // Unused property since the watermark should not be clickable in a native environment.
    var watermarkTargetLink: String? = null

    /**
     * Affects the given [StyleConfigStream] with the values of this [StyleConfig].
     * This only affects the [PlayerStyle] of the [StyleConfigStream] since the watermark is only customizable from the dashboard for now.
     */
    internal fun affectConfig(style : StyleConfigStream) {
        playerStyle.affectConfig(style)
    }
}

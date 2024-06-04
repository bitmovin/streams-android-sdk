package com.bitmovin.streams.streamsjson

import com.bitmovin.streams.StyleConfigStream


class StyleConfig {
    var playerStyle: PlayerStyle = PlayerStyle()
    var watermarkUrl: String? = null
    var watermarkTargetLink: String? = null

    internal fun affectConfig(style : StyleConfigStream) {
        playerStyle.affectConfig(style.playerStyleConfigStream)
        style.watermarkUrl?.let {
            watermarkUrl = it
        }
    }
}

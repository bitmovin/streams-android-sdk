package com.bitmovin.streams.streamsjson

/**
 * This class is used to store the configuration data of a stream.
 */
internal class StreamConfigData {
    lateinit var key: String
    lateinit var sources: Sources
    var analytics: Analytics = Analytics()
    lateinit var type: String // VIDEO | LIVE
    var styleConfig: StyleConfig = StyleConfig()
    var adConfig: AdConfig = AdConfig()

    fun isLive(): Boolean {
        return type == "LIVE"
    }
}

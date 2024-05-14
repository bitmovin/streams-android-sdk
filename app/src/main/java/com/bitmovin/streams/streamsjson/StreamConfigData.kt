package com.bitmovin.streams.streamsjson

/***
 * This class is used to store the configuration data of a stream.
 */
class StreamConfigData {
    lateinit var key: String
    lateinit var sources: Sources
    lateinit var analytics: Analytics
    lateinit var type: String // VIDEO | LIVE
    var styleConfig: StyleConfig? = null
    var adConfig: AdConfig? = null
}
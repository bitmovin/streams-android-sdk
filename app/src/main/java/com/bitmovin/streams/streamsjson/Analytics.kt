package com.bitmovin.streams.streamsjson

import com.bitmovin.analytics.api.AnalyticsConfig
import com.bitmovin.player.api.analytics.AnalyticsPlayerConfig

class Analytics {
    private lateinit var key: String
    lateinit var videoId: String
    lateinit var videoTitle: String

    fun getAnalyticsConfig() : AnalyticsConfig {
        return AnalyticsConfig(
            licenseKey = key
        )
    }
}
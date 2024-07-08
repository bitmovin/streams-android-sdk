package com.bitmovin.streams

import androidx.compose.ui.graphics.Color
import com.bitmovin.player.api.advertising.AdSourceType
import com.bitmovin.streams.streamsjson.StreamConfigData
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class CorrectnessTest {
    @Test
    fun createAdsFromStreamData() {
        val streamData = Gson().fromJson(tearOfSteel, StreamConfigData::class.java)

        val adConfig =
            createAdvertisingConfig(
                streamData,
            )
        assert(adConfig.schedule.size == 3)
        adConfig.schedule.forEachIndexed { index, ad ->
            assertEquals(ad.position, streamData.adConfig.ads[index].position)
            assertEquals(ad.sources[0].tag, streamData.adConfig.ads[index].url)
            when (index) {
                0 -> assertEquals(ad.sources[0].type, AdSourceType.Progressive)
                1 -> assertEquals(ad.sources[0].type, AdSourceType.Bitmovin)
                2 -> assertEquals(ad.sources[0].type, AdSourceType.Bitmovin)
            }
        }
    }

    /**
     * Tests quickly Color.toCSS() & String.parseColor()
     * Might be useful to delegate to a library to handle every case
     */
    @Test
    fun colorCssNative() {
        val color = Color(255, 0, 0, 255)
        assertEquals(color.toCSS(), "rgba(255, 0, 0, 1.0)")
        assertEquals(Color.parseColor("rgba(255, 0, 0, 1.0)"), color)
    }

    @Test
    fun createSourceConfigFromStreamData() {
        val streamData = Gson().fromJson(tearOfSteel, StreamConfigData::class.java)

        val source =
            createSourceConfig(
                streamData,
                null,
            )
        assertEquals(source.url, streamData.sources.hls)
        assertEquals(source.posterSource, streamData.sources.poster)
        assertEquals(source.thumbnailTrack?.url, streamData.sources.thumbnailTrack.url)
        assertEquals(source.title, streamData.sources.title)
    }

    @Test
    fun createSourceMetaDataFromStreamData() {
        val streamData = Gson().fromJson(tearOfSteel, StreamConfigData::class.java)

        val meta =
            createMetadata(
                streamData,
            )

        assertEquals(meta.videoId, streamData.analytics.videoId)
        assertEquals(meta.title, streamData.analytics.videoTitle)
        assertEquals(meta.isLive, streamData.isLive())
    }
}

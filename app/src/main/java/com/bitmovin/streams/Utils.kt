package com.bitmovin.streams

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.bitmovin.analytics.api.AnalyticsConfig
import com.bitmovin.analytics.api.SourceMetadata
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.PlayerConfig
import com.bitmovin.player.api.advertising.AdItem
import com.bitmovin.player.api.advertising.AdSource
import com.bitmovin.player.api.advertising.AdSourceType
import com.bitmovin.player.api.advertising.AdvertisingConfig
import com.bitmovin.player.api.analytics.AnalyticsPlayerConfig
import com.bitmovin.player.api.analytics.AnalyticsSourceConfig
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.player.api.source.Source
import com.bitmovin.player.api.source.SourceConfig
import com.bitmovin.player.api.source.SourceType
import com.bitmovin.player.api.ui.PlayerViewConfig
import com.bitmovin.player.api.ui.UiConfig
import com.bitmovin.streams.streamsjson.StreamConfigData
import com.bitmovin.streams.streamsjson.StreamConfigDataResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Fetches the stream config data from the Bitmovin API
 * @param streamId the id of the stream
 * @return the stream config data
 * @throws IOException if the request fails
 */
suspend fun getStreamConfigData(streamId: String, jwToken: String?) : StreamConfigDataResponse {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        var url = "https://streams.bitmovin.com/${streamId}/config"
        if (jwToken != null) {
            url = addURLParam(url, "token", jwToken)
        }
        val request: Request = Request.Builder()
            .url(url)
            .build()

        Log.d("Streams", "Request: $request")
        val response = client.newCall(request).execute()
        val code = response.code
        if (!response.isSuccessful) {
            return@withContext StreamConfigDataResponse(null, code)
        }
        val responseBody = response.body?.string()
        val streamConfigData = Gson().fromJson(responseBody, StreamConfigData::class.java)
        return@withContext StreamConfigDataResponse(streamConfigData, code)
    }
}

fun addURLParam(url: String, attribute: String, value: String): String {
    val separator = if (url.contains("?")) "&" else "?"
    return "$url$separator$attribute=$value"
}

fun createPlayer(streamConfigData: StreamConfigData, context: Context): Player {
    val analyticsConfig : AnalyticsConfig = streamConfigData.analytics.getAnalyticsConfig()
    val advertisingConfig : AdvertisingConfig = getAdvertisingConfig(streamConfigData)
    val playerConfig : PlayerConfig = PlayerConfig(
        key = streamConfigData.key,
        advertisingConfig = advertisingConfig,
    )

    return Player(
        context,
        playerConfig = playerConfig,
        analyticsConfig = AnalyticsPlayerConfig.Enabled(analyticsConfig),
    )
}
fun getAdvertisingConfig(streamConfig: StreamConfigData): AdvertisingConfig {
    // Bitmovin and Progressive ads only for now
    val ads = streamConfig.adConfig?.ads?.map { ad ->
        val fileExt = ad.url.split(".").last()
        val adSource = when (fileExt) {
            "mp4" -> AdSource(AdSourceType.Progressive, ad.url)
            "xml" -> AdSource(AdSourceType.Bitmovin, ad.url)
            // "..." -> AdSource(AdSourceType.Ima, ad.url) ignoring this case for now
            else -> AdSource(AdSourceType.Unknown, ad.url)
        }
        AdItem(ad.position, adSource)
    } ?: emptyList()

    if (ads.isEmpty()) {
        Log.d("Ads", "No ads found")
        return AdvertisingConfig()
    }
    return AdvertisingConfig(ads)
}



fun createSource(streamConfigData: StreamConfigData, customPosterSource: String?, subtitlesSources: List<SubtitleTrack> = emptyList()): Source {
    val sourceConfig = SourceConfig(
        url = streamConfigData.sources.hls,
        type = SourceType.Hls, // Might be different in some cases but let's pretend it's always HLS for now
        title = streamConfigData.sources.title,
        posterSource = customPosterSource?: streamConfigData.sources.poster,
        subtitleTracks = subtitlesSources,
    )
    val sourceMetadata = SourceMetadata(
        videoId = streamConfigData.analytics.videoId,
        title = streamConfigData.analytics.videoTitle,
    )
    return Source(
        sourceConfig,
        AnalyticsSourceConfig.Enabled(sourceMetadata)
    )
}

fun createPlayerView(context: Context, player: Player) : PlayerView{
    val playerView = PlayerView(context, player, config =
    PlayerViewConfig(
        UiConfig.WebUi(
            // Should be chqnged once the endpoint is ready
            jsLocation =  "https://cdn.bitmovin.com/player/web/8/bitmovinplayer-ui.js",
            cssLocation = "https://cdn.bitmovin.com/player/web/8/bitmovinplayer-ui.css",
        ))
    ).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        keepScreenOn = true
    }
    return playerView
}
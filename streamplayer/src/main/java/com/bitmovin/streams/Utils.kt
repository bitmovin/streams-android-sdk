package com.bitmovin.streams

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
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
import kotlin.reflect.KProperty

/**
 * Removes the view from its parent.
 */
internal fun FrameLayout.removeFromParent() {
    this.parent?.let {
        (it as? ViewGroup)?.removeView(this)
    }
}


// Window getter utils for Composable, credits goes to the stackoverflow-guy
@Composable
internal fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window

@Composable
internal fun getActivityWindow(): Window? = LocalView.current.context.getActivityWindow()

private tailrec fun Context.getActivityWindow(): Window? =
    when (this) {
        is Activity -> window
        is ContextWrapper -> baseContext.getActivityWindow()
        else -> null
    }



/**
 * Getting the Activity from a random Context
 * (Home made, need to be tested with different contexts types)
 */
internal fun Context.getActivity(): Activity? {
    val context = this
    if (context is ContextWrapper) {
        return if (context is Activity) {
            context
        } else {
            context.baseContext.getActivity()
        }
    }
    return null
}


internal suspend fun getStreamConfigData(streamId: String, jwToken: String?) : StreamConfigDataResponse {
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

internal fun addURLParam(url: String, attribute: String, value: String): String {
    val separator = if (url.contains("?")) "&" else "?"
    return "$url$separator$attribute=$value"
}

internal fun createPlayer(streamConfigData: StreamConfigData, context: Context): Player {
    val analyticsConfig : AnalyticsConfig? = getAnalyticsConfig(streamConfigData)
    val advertisingConfig : AdvertisingConfig = getAdvertisingConfig(streamConfigData)
    val playerConfig = PlayerConfig(
        key = streamConfigData.key,
        advertisingConfig = advertisingConfig,
    )

    return if (analyticsConfig == null) {
        Player(
            context,
            playerConfig = playerConfig,
        )
    } else Player(
        context,
        playerConfig = playerConfig,
        analyticsConfig = AnalyticsPlayerConfig.Enabled(analyticsConfig),
    )
}
internal fun getAdvertisingConfig(streamConfig: StreamConfigData): AdvertisingConfig {
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

internal fun getAnalyticsConfig(streamConfig: StreamConfigData) : AnalyticsConfig? {
    streamConfig.analytics.let { analytics ->
        if (analytics == null) {
            return null
        } else {
            return AnalyticsConfig(
                licenseKey = analytics.key
            )
        }
    }
}



internal fun createSource(streamConfigData: StreamConfigData, customPosterSource: String?, subtitlesSources: List<SubtitleTrack> = emptyList()): Source {
    val sourceConfig = SourceConfig(
        url = streamConfigData.sources.hls,
        type = SourceType.Hls, // Might be different in some cases but let's pretend it's always HLS for now
        title = streamConfigData.sources.title,
        posterSource = customPosterSource?: streamConfigData.sources.poster,
        subtitleTracks = subtitlesSources,
    )
    val sourceMetadata = SourceMetadata(
        videoId = streamConfigData.analytics?.videoId,
        title = streamConfigData.analytics?.videoTitle,
        isLive = streamConfigData.type == "LIVE",
    )
    return Source(
        sourceConfig,
        AnalyticsSourceConfig.Enabled(sourceMetadata)
    )
}

internal fun createPlayerView(context: Context, player: Player, jsLocation: String? = null, cssLocation : String? = null) : PlayerView{
    var playerViewConfig = PlayerViewConfig(UiConfig.WebUi(forceSubtitlesIntoViewContainer = true))
    if (jsLocation != null && cssLocation != null) {
        playerViewConfig = PlayerViewConfig(
            UiConfig.WebUi(
                jsLocation = jsLocation,
                cssLocation = cssLocation,
                forceSubtitlesIntoViewContainer = true
            )
        )
    } else if (jsLocation != null) {
        playerViewConfig = PlayerViewConfig(
            UiConfig.WebUi(
                jsLocation = jsLocation,
                forceSubtitlesIntoViewContainer = true
            )
        )
    } else if (cssLocation != null) {
        playerViewConfig = PlayerViewConfig(
            UiConfig.WebUi(
                cssLocation = cssLocation,
                forceSubtitlesIntoViewContainer = true
            )
        )
    }

    val playerView = PlayerView(context, player, config = playerViewConfig)
        .apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        keepScreenOn = true
    }
    return playerView
}

internal operator fun String.getValue(nothing: Nothing?, property: KProperty<*>): String {
    return this
}

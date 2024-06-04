package com.bitmovin.streams

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import com.bitmovin.analytics.api.AnalyticsConfig
import com.bitmovin.analytics.api.CustomData
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
import com.bitmovin.streams.streamsjson.PlayerStyle
import com.bitmovin.streams.streamsjson.StreamConfigData
import com.bitmovin.streams.streamsjson.StreamConfigDataResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

internal fun createPlayer(streamConfigData: StreamConfigData, context: Context, enableAds: Boolean): Player {
    val analyticsConfig : AnalyticsConfig = getAnalyticsConfig(streamConfigData)
    val advertisingConfig : AdvertisingConfig? =
        when (enableAds) {
            true -> getAdvertisingConfig(streamConfigData)
            false -> null
        }

    val playerConfig = PlayerConfig(
        key = streamConfigData.key,
        advertisingConfig = advertisingConfig?: AdvertisingConfig(),
    )

    return Player(
        context,
        playerConfig = playerConfig,
        analyticsConfig = AnalyticsPlayerConfig.Enabled(analyticsConfig)
    )
}
internal fun getAdvertisingConfig(streamConfig: StreamConfigData): AdvertisingConfig {
    // Bitmovin and Progressive ads only for now
    val ads = streamConfig.adConfig.ads.map { ad ->
        val fileExt = ad.url.split(".").last()
        val adSource = when (fileExt) {
            "mp4" -> AdSource(AdSourceType.Progressive, ad.url)
            "xml" -> AdSource(AdSourceType.Bitmovin, ad.url)
            // "..." -> AdSource(AdSourceType.Ima, ad.url) ignoring this case for now
            else -> AdSource(AdSourceType.Unknown, ad.url)
        }
        AdItem(ad.position, adSource)
    } ?: emptyList()
    return AdvertisingConfig(ads)
}

internal fun getAnalyticsConfig(streamConfig: StreamConfigData) : AnalyticsConfig {
    return AnalyticsConfig(
        streamConfig.analytics.key,
    )
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
        videoId = streamConfigData.analytics.videoId,
        title = streamConfigData.analytics.videoTitle,
        isLive = streamConfigData.type == "LIVE",
        customData = CustomData("STREAMS-ANDROID-COMPONENT"),
    )
    return Source(
        sourceConfig,
        AnalyticsSourceConfig.Enabled(sourceMetadata)
    )
}

internal fun createPlayerView(context: Context, player: Player, streamConfig : StreamConfigData, styleConfigStream: StyleConfigStream) : PlayerView{

    // Should be done at the beginning or the attributes values will be ignored.
    streamConfig.styleConfig.affectConfig(styleConfigStream)

    val suppCssLocation = streamConfig.let { getCustomCss(context, it.key, it) }
    val playerViewConfig = PlayerViewConfig(
            UiConfig.WebUi(
                supplementalCssLocation = suppCssLocation,
                forceSubtitlesIntoViewContainer = true,
            )
        )

    val playerView = PlayerView(context, player, config = playerViewConfig)
        .apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        keepScreenOn = true
    }
    // Should be done at the end
    streamConfig.styleConfig.playerStyle.backgroundColor?.let {
        it.parseColor()?.toArgb()?.let { colorInt -> playerView.setBackgroundColor(colorInt) ; Log.d("Color", "Setting ALEERR color to $colorInt") }
    }
    return playerView
}

internal operator fun String.getValue(nothing: Nothing?, property: KProperty<*>): String {
    return this
}


internal fun writeCssToFile(context: Context, css: String, streamId: String): File? {
    return try {
        // Create a file in the app's private storage
        val cssFile = File(context.filesDir, "custom_css_${streamId}.css")
        Log.d("CSS", "Writing CSS to file: $cssFile")
        if (cssFile.exists()) {
            cssFile.delete()
        }
        // Write the CSS content to the file
        FileOutputStream(cssFile).use { output ->
            output.write(css.toByteArray())
        }
        cssFile
    } catch (e: IOException) {
        null
    }
}

internal fun getCustomCss(context : Context, id : String, streamConfig: StreamConfigData) : String {

    val style = streamConfig.styleConfig
    val css = StringBuilder()



    style.playerStyle.let {
        css.append(playerStyle(it))
    }
    style.watermarkUrl?.let {
        css.append(watermarkCss(it))
    } ?: {
        style
    }

    Log.d("CSS", "Writing CSS to file: \n$css")

    return writeCssToFile(context, css.toString(), id)?.toURL().toString()
}



internal fun watermarkCss(watermarkImg: String) : String {
    return """
        .bmpui-ui-watermark {
            background-image: url("$watermarkImg") !important;
            background-size: contain !important;
            background-repeat: no-repeat !important;
            background-position: center center !important;
            font-size: .7em !important;
            display: block !important;
            pointer-events: none !important;
            top: 20px !important;
            opacity: 1 !important;
            transition: opacity 0.5s ease, top 0.5s ease !important;
        }
        .bmpui-controls-hidden .bmpui-ui-watermark {
            top: 0px !important;
            opacity: 0.2 !important;
        }
    """.trimIndent()
}

internal fun playerStyle(playerStyle: PlayerStyle) : String
{
    val playerStyles : StringBuilder = StringBuilder()
    playerStyle.playbackMarkerBgColor?.let {
        playerStyles.append(stylePlaybackMarkerBgColor(it))
    }
    playerStyle.playbackMarkerBorderColor?.let {
        playerStyles.append(stylePlaybackMarkerBorderColor(it))
    }
    playerStyle.playbackTrackPlayedColor?.let {
        playerStyles.append(stylePlaybackTrackPlayedColor(it))
    }
    playerStyle.playbackTrackBufferedColor?.let {
        playerStyles.append(stylePlaybackTrackBufferedColor(it))
    }
    playerStyle.playbackTrackBgColor?.let {
        playerStyles.append(stylePlaybackTrackBgColor(it))
    }
    playerStyle.textColor?.let {
        playerStyles.append(styleTextColor(it))
    }

    return playerStyles.toString()
}



internal fun stylePlaybackMarkerBgColor(color: String) : String
{
    // Volume slider is not used in this playerView but I based the impl on the web player UI.
    return """
        .bmpui-ui-seekbar .bmpui-seekbar .bmpui-seekbar-playbackposition-marker,
        .bmpui-ui-volumeslider .bmpui-seekbar .bmpui-seekbar-playbackposition-marker {
           background-color: $color !important;
        }
    """.trimIndent()
}

internal fun stylePlaybackMarkerBorderColor(color: String): String {
    // Volume slider is not used in this playerView but I based the impl on the web player UI.
    return """
        .bmpui-ui-seekbar .bmpui-seekbar .bmpui-seekbar-playbackposition-marker,
        .bmpui-ui-volumeslider .bmpui-seekbar .bmpui-seekbar-playbackposition-marker {
           border-color: $color !important;
        }
    """.trimIndent()
}

internal fun stylePlaybackTrackPlayedColor(color: String): String {
    return """
        .bmpui-seekbar .bmpui-seekbar-playbackposition {
           background-color: $color !important;
        }
    """.trimIndent()
}

internal fun stylePlaybackTrackBufferedColor(color: String): String {
    return """
        .bmpui-seekbar .bmpui-seekbar-bufferlevel {
           background-color: $color !important;
        }
    """.trimIndent()
}


internal fun stylePlaybackTrackBgColor(color: String): String {
    return """
        .bmpui-seekbar .bmpui-seekbar-backdrop {
           background-color: $color !important;
        }
    """.trimIndent()
}

internal fun styleTextColor(color: String): String {
    return """ 
        .bmpui-ui-playbacktimelabel, 
        .bmpui-ui-titlebar {
           color: $color !important;
        }r
    """.trimIndent()
}

fun Color.toCSS() : String {
    val s = "rgba(${(this.red*255).toInt()}, ${(this.green*255).toInt()}, ${(this.blue*255).toInt()}, ${this.alpha})"
    Log.d("Color", "Converting color to CSS: $s")
    return s
}


// Credits to https://stackoverflow.com/questions/12643009/regular-expression-for-floating-point-numbers
const val floatNumber = "([+-]?([0-9]+([.][0-9]*)?|[.][0-9]+))"

// TODO: Add support for hex colors
// TODO: Change the parsing without using Regex because it seems overkill
fun String.parseColor() : Color? {
    Log.d("Color", "Parsing color from string: $this")
    val pattern = "rgba\\((\\d+), (\\d+), (\\d+), $floatNumber\\)".toRegex()
    val match = pattern.find(this)
    if (match != null) {
        val (r,g,b,a) = match.destructured
        return Color(r.toInt(), g.toInt(), b.toInt(), (a.toFloat()*255).toInt())
    } else {
        Log.e("Color", "Failed to parse color from string: $this")
    }
    return null
}
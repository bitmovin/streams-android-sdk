package com.bitmovin.streams

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import com.bitmovin.player.api.media.thumbnail.ThumbnailTrack
import com.bitmovin.player.api.source.Source
import com.bitmovin.player.api.source.SourceConfig
import com.bitmovin.player.api.source.SourceType
import com.bitmovin.player.api.ui.PlayerViewConfig
import com.bitmovin.player.api.ui.UiConfig
import com.bitmovin.streams.config.StyleConfigStream
import com.bitmovin.streams.streamsjson.PlayerStyle
import com.bitmovin.streams.streamsjson.StreamConfigData
import com.bitmovin.streams.streamsjson.StreamConfigDataResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Thread.sleep
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
internal fun getDialogWindow(): Window? =
    (LocalView.current.parent as? DialogWindowProvider)?.window

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

const val MAX_FETCHING_ATTEMPTS = 3
internal suspend fun getStreamConfigData(
    streamId: String,
    jwToken: String?,
    logger: Logger
): StreamConfigDataResponse {
    return withContext(Dispatchers.IO) {
        return@withContext logger.recordDuration("Fetching stream config data") {
            val client = StreamsProvider.okHttpClient
            var url = "https://streams.bitmovin.com/${streamId}/config"
            if (jwToken != null) {
                url = addURLParam(url, "token", jwToken)
            }
            val request: Request = Request.Builder()
                .url(url)
                .build()

            logger.d("Request: $request")
            var response : Response
            var trys = 0
            do {
                sleep((200 * trys).toLong())
                try {
                    response = client.newCall(request).execute()
                } catch (e: Exception) {
                    return@recordDuration StreamConfigDataResponse(null, 0)
                }
                trys++
            } while (response.code != 200 && trys <= MAX_FETCHING_ATTEMPTS)
            val code = response.code
            if (!response.isSuccessful) {
                StreamConfigDataResponse(null, code)
            }
            // This is not risky since the content should not be too big.
            val responseBody = response.body?.string()
            val streamConfigData = Gson().fromJson(responseBody, StreamConfigData::class.java)
            StreamConfigDataResponse(streamConfigData, code)
        }
    }
}

internal fun addURLParam(url: String, attribute: String, value: String): String {
    val separator = if (url.contains("?")) "&" else "?"
    return "$url$separator$attribute=$value"
}

internal fun createPlayer(
    streamConfigData: StreamConfigData,
    context: Context,
    enableAds: Boolean,
    logger: Logger
): Player {
    val analyticsConfig: AnalyticsConfig = createAnalyticsConfig(streamConfigData)
    val advertisingConfig: AdvertisingConfig? =
        when (enableAds) {
            true -> createAdvertisingConfig(streamConfigData)
            false -> null
        }

    val playerConfig = PlayerConfig(
        key = streamConfigData.key,
        advertisingConfig = advertisingConfig ?: AdvertisingConfig(),
    )

    return logger.recordDuration("Creating player") {
        // From the countTime function, we can see that the creation of the Player is the most expensive part of the process by really far.
        // I unfortunately can't do much about it since it's part of the Bitmovin SDK.
        // It also has to be done on the main thread.
        Player(
            context,
            playerConfig = playerConfig,
            analyticsConfig = AnalyticsPlayerConfig.Enabled(analyticsConfig)
        )
    }
}

internal fun createAdvertisingConfig(streamConfig: StreamConfigData): AdvertisingConfig {
    // Bitmovin and Progressive ads only for now
    val ads = streamConfig.adConfig.ads.map { ad ->
        val fileExt = ad.url.split(".").last()
        val adSource = when (fileExt) {
            // From testing, it seems mp4 is the only supported format for Progressive ads.
            // I didn't find any documentation about how to automatically detect the type of ad source.
            "mp4" -> AdSource(AdSourceType.Progressive, ad.url)
            "xml" -> AdSource(AdSourceType.Bitmovin, ad.url)
            // "..." -> AdSource(AdSourceType.Ima, ad.url) ignoring this case for now
            // Does not support IMAs.
            // If nothing is detected, let's try as a Bitmovin ad. It's common and should not affect negatively the player if it fails.
            else -> AdSource(AdSourceType.Bitmovin, ad.url)
        }
        AdItem(ad.position, adSource)
    }
    return AdvertisingConfig(ads)
}

internal fun createAnalyticsConfig(streamConfig: StreamConfigData): AnalyticsConfig {
    return AnalyticsConfig(
        streamConfig.analytics.key,
    )
}

internal fun createSourceConfig(
    streamConfigData: StreamConfigData,
    customPosterSource: String?,
    subtitlesSources: List<SubtitleTrack> = emptyList()
): SourceConfig {
    return SourceConfig(
        url = streamConfigData.sources.hls,
        type = SourceType.Hls, // Always HLS since Streams only supports HLS for now
        title = streamConfigData.sources.title,
        posterSource = customPosterSource ?: streamConfigData.sources.poster,
        subtitleTracks = subtitlesSources,
        thumbnailTrack = streamConfigData.sources.thumbnailTrack.url?.let { ThumbnailTrack(it) }
    )
}

internal fun createMetadata(
    streamConfigData: StreamConfigData
): SourceMetadata {
    return SourceMetadata(
        videoId = streamConfigData.analytics.videoId,
        title = streamConfigData.analytics.videoTitle,
        isLive = streamConfigData.isLive(),
        customData = CustomData("STREAMS-ANDROID-COMPONENT"),
    )
}

internal fun createSource(
    streamConfigData: StreamConfigData,
    customPosterSource: String?,
    subtitlesSources: List<SubtitleTrack> = emptyList()
): Source {
    val sourceConfig = createSourceConfig(streamConfigData, customPosterSource, subtitlesSources)
    val sourceMetadata = createMetadata(streamConfigData)

    return Source(
        sourceConfig,
        AnalyticsSourceConfig.Enabled(sourceMetadata)
    )
}

internal suspend fun createPlayerView(
    context: Context,
    player: Player,
    streamConfig: StreamConfigData,
    styleConfigStream: StyleConfigStream,
    styleFileKey: String,
    logger: Logger
): PlayerView {

    // Should be done at the beginning or the attributes values will be ignored.
    streamConfig.styleConfig.affectConfig(styleConfigStream)


    val suppCssLocation = withContext(Dispatchers.IO) {
        return@withContext logger.recordDuration("Writting Css rules") {
            getCustomCss(
                streamConfig,
                userSupplCss = styleConfigStream.customCss,
                styleFileKey,
                logger
            )
        }
    }
    val playerViewConfig = PlayerViewConfig(
        UiConfig.WebUi(
            supplementalCssLocation = suppCssLocation,
            forceSubtitlesIntoViewContainer = true,
        )
    )
    /*
    Ideally, the creation of the PlayerView should be done on an IO thread since it's quicker and
    doesn't require the main thread (that's what I thought at least)
    But there is some rare cases where creating the PlayerView on an IO thread will cause
    the WebView to not show the fullscreen button. TODO: Investigate why with the Player Jedis
    However, since the perf anyway only really matters on launch on the fist time (because of cache)
    It is better and safer to keep it on the main thread for now.
     */
    val playerView = logger.recordDuration("PlayerView Creation") {
            PlayerView(context, player, config = playerViewConfig)
                .apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    keepScreenOn = true
                }
        }
    // Should be done at the end
    //TODO: Make the background in the webview be affected too to avoid having to wait the video start to change the background.
    streamConfig.styleConfig.playerStyle.backgroundColor?.let {
        Color.parseColor(it)?.toArgb()?.let { colorInt -> playerView.setBackgroundColor(colorInt) }
    }
    return playerView
}

internal operator fun String.getValue(nothing: Nothing?, property: KProperty<*>): String {
    return this
}

/**
 * Create a file in the app's private storage and write the CSS content to it.
 * @param fileKey The unique key to identify the file.
 * @param context The context of the app.
 * @param css The CSS content to write to the file.
 * @return The file if the operation was successful, null otherwise.
 */
internal fun writeCssToFile(fileKey: String, context: Context, css: String, logger: Logger): File? {
    return try {
        // Create a file in the app's private storage
        val cssFile = File(context.filesDir, "custom_css_${fileKey}.css")
        if (cssFile.exists()) {
            cssFile.delete()
        }
        // Write the CSS content to the file
        FileOutputStream(cssFile).use { output ->
            output.write(css.toByteArray())
        }
        logger.d("Writing CSS rules to file: $cssFile")
        cssFile
    } catch (e: IOException) {
        logger.e(
            "Failed to write CSS rules to file. Stylization rules will be ignored.",
            e
        )
        null
    }
}

/**
 * Get the URL of the CSS file.
 * @param streamConfig The stream configuration data.
 * @param userSupplCss The user-supplied CSS content.
 * @param usid The unique key to identify the file.
 *
 */
internal fun getCustomCss(
    streamConfig: StreamConfigData,
    userSupplCss: String,
    usid: String,
    logger: Logger
): String {

    val style = streamConfig.styleConfig
    val css = StringBuilder()

    style.playerStyle.let {
        css.append(playerStyle(it))
    }
    style.watermarkUrl?.let {
        css.append(watermarkCss(it))
    }
    css.append("\n$userSupplCss")

    @Suppress("DEPRECATION")
    return writeCssToFile(usid, StreamsProvider.appContext, css.toString(), logger = logger)?.toURL()
        .toString()
}

/*
    CSS ZONE
*/

internal fun playerStyle(playerStyle: PlayerStyle): String {
    val playerStyles: StringBuilder = StringBuilder()
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
    playerStyle.backgroundColor?.let {
        // Not working for now
        playerStyles.append(backgroundColor(it))
    }
    return playerStyles.toString()
}

internal fun watermarkCss(watermarkImg: String): String {
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
            transition: opacity 0.3s ease, top 0.5s ease-out !important;
        }
        .bmpui-controls-hidden .bmpui-ui-watermark {
            top: -16px !important;
            opacity: 0.2 !important;
        }
        
        :has(.bmpui-ui-settingstogglebutton.bmpui-on) .bmpui-ui-watermark {
            opacity: 0.0 !important;
        }
    """.trimIndent()
}

internal fun stylePlaybackMarkerBgColor(color: String): String {
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
        }
        
    """.trimIndent()
}

internal fun backgroundColor(color: String): String {
    return """ 
        .bitmovin-stream-wrapper
        .bitmovinplayer-container,
         .bitmovinplayer-poster {
           background-color: $color !important;
        }
        /*  
        Make the gradiant when pause  accord to the background color but I'm not sure if it's really a bahavior we want.
                .bmpui-ui-titlebar {
                    background: -webkit-gradient(linear, left bottom, left top, from(transparent), to($color));
                }
        */
        .bitmovin-player {
            background-color: $color !important;
        }
    """.trimIndent()
}

internal fun Color.toCSS(): String {
    val s =
        "rgba(${(this.red * 255).toInt()}, ${(this.green * 255).toInt()}, ${(this.blue * 255).toInt()}, ${this.alpha})"
    return s
}

internal fun Color.Companion.parseColor(str: String): Color? {
    try {
        val c = org.silentsoft.csscolor4j.Color.valueOf(str)
        return Color(c.red, c.green, c.blue, (c.opacity*255).toInt())
    } catch(e: IllegalArgumentException) {
        return null
    }
}

internal fun getLoadingScreenMessage(state: BitmovinStreamState): String {
    return when (state) {
        BitmovinStreamState.FETCHING -> "Fetching stream config data"
        BitmovinStreamState.INITIALIZING -> "Initializing player"
        BitmovinStreamState.WAITING_FOR_VIEW -> "Waiting for player view"
        BitmovinStreamState.WAITING_FOR_PLAYER -> "Waiting for player"
        else -> "An error occurred while fetching the stream data."
    }
}

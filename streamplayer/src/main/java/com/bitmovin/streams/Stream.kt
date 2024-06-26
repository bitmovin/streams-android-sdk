package com.bitmovin.streams

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import com.bitmovin.player.PlayerView
import com.bitmovin.player.SubtitleView
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.event.PlayerEvent
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.player.api.ui.FullscreenHandler
import com.bitmovin.streams.config.BitmovinStreamEventListener
import com.bitmovin.streams.config.FullscreenConfig
import com.bitmovin.streams.config.StyleConfigStream
import com.bitmovin.streams.fullscreenmode.StreamFullscreenHandler
import com.bitmovin.streams.pipmode.PiPExitListener
import com.bitmovin.streams.pipmode.PiPHandler
import com.bitmovin.streams.streamsjson.StreamConfigData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Thread.sleep

internal class Stream(private val usid: String, allLogs: Boolean = false) {
    val logger = Logger(usid, allLogs)
    var state by mutableStateOf(BitmovinStreamState.FETCHING)
    var streamError = StreamError.UNKNOWN_ERROR
    var playerView: PlayerView? = null
    var player: Player? = null
    private var streamEventListener: BitmovinStreamEventListener? = null
    private var pipExitHandler: PiPExitListener? = null

    /**
     * Will do both the fetching and the initialization of the stream
     */
    fun initStream(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        streamId: String,
        jwToken: String?,
        autoPlay: Boolean,
        loop: Boolean,
        muted: Boolean,
        start: Double,
        poster: String?,
        subtitles: List<SubtitleTrack>,
        fullscreenConfig: FullscreenConfig,
        enableAds: Boolean,
        styleConfigStream: StyleConfigStream,
        bitmovinStreamEventListener: BitmovinStreamEventListener?
    ) {
        logger.i("Initializing stream $streamId")
        state = BitmovinStreamState.FETCHING
        this.streamEventListener = bitmovinStreamEventListener
        CoroutineScope(Dispatchers.Main).launch {
            val streamConfigDataResp = getStreamConfigData(streamId, jwToken, logger)
            val streamResponseCode = streamConfigDataResp.responseHttpCode
            if (streamResponseCode == 200 && streamConfigDataResp.streamConfigData != null) {
                initializeStream(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    streamConfig = streamConfigDataResp.streamConfigData,
                    autoPlay = autoPlay,
                    loop = loop,
                    muted = muted,
                    start = start,
                    poster = poster,
                    subtitles = subtitles,
                    fullscreenConfig = fullscreenConfig,
                    enableAds = enableAds,
                    styleConfigStream = styleConfigStream
                )
            } else {
                castError(StreamError.fromHttpCode(streamResponseCode))
            }
        }
    }

    private fun castError(error: StreamError) {
        streamError = error
        logger.e("$streamError")
        streamEventListener?.onStreamError(error)
        state = BitmovinStreamState.DISPLAYING_ERROR
    }

    /**
     * Initialize the player with the stream config data and Stream Player attributes
     * Should be called after the stream config data has been fetched.
     */
    private suspend fun initializeStream(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        streamConfig: StreamConfigData,
        autoPlay: Boolean,
        loop: Boolean,
        muted: Boolean,
        start: Double,
        poster: String?,
        subtitles: List<SubtitleTrack>,
        fullscreenConfig: FullscreenConfig,
        enableAds: Boolean,
        styleConfigStream: StyleConfigStream
    ) {
        state = BitmovinStreamState.INITIALIZING
        StreamsProvider.getInstance().pipChangesObserver.let {
            it.context = context
            lifecycleOwner.lifecycle.addObserver(it)
        }

        // 1. Initializing the player
        val player = initializePlayerRelated(
            context,
            streamConfig,
            enableAds
        )

        // 2. Initializing the views
        initializeViewRelated(
            context,
            player,
            lifecycleOwner,
            streamConfig,
            styleConfigStream,
            fullscreenConfig
        )

        // 3. Loading the source
        // Warning: Running this block in the IO dispatcher could results in a crash (RuntimeException) if the component disappears before the end (because of the disposal effects)
        // This is also a lot faster on the main thread. However, it's a huge tradeoff because it's blocking the UI thread. No choice since we can't handle a RuntimeException.
        logger.recordDuration("Loading Source") {
            val streamSource =
                createSource(
                    streamConfig,
                    customPosterSource = poster,
                    subtitlesSources = subtitles
                )
            player.load(streamSource)
        }
        // 4. Handling properties
        // Warning: The stream source has to be loaded before setting the properties. This is why we do it here.
        // PlayerEvent.Ready event not be called before the source is loaded.
        player.handleAttributes(autoPlay, muted, loop && !streamConfig.isLive(), start)
    }

    private fun initializePlayerRelated(
        context: Context,
        streamConfig: StreamConfigData,
        enableAds: Boolean,
    ): Player {
        val player = createPlayer(streamConfig, context, enableAds, logger)
        this.player = player
        player.on(PlayerEvent.Ready::class.java) {
            if (state == BitmovinStreamState.INITIALIZING) {
                state = BitmovinStreamState.WAITING_FOR_VIEW
            } else if (state == BitmovinStreamState.WAITING_FOR_PLAYER) {
                state = BitmovinStreamState.DISPLAYING
                streamEventListener?.onStreamReady(player, playerView!!)
            }
        }
        // If the source loading crashes while initializing, the stream is blocked
        player.on(PlayerEvent.SourceRemoved::class.java) {
            if (state != BitmovinStreamState.DISPLAYING) {
                castError(StreamError.SOURCE_ERROR)
            }
        }

        return player
    }


    private suspend fun initializeViewRelated(
        context: Context,
        player: Player,
        lifecycleOwner: LifecycleOwner,
        streamConfig: StreamConfigData,
        styleConfigStream: StyleConfigStream,
        fullscreenConfig: FullscreenConfig
    ) {
        val activity = context.getActivity()

        // Setting up the player view
        playerView =
            createPlayerView(context, player, streamConfig, styleConfigStream, usid, logger)
        val playerView = playerView!!
        // Adding the playerView to the lifecycle
        lifecycleOwner.lifecycle.addObserver(
            LifeCycleRedirectForPlayer(
                playerView,
                fullscreenConfig.autoPiPOnBackground
            )
        )
        // Setting up the subtitles view
        val subtitlesView = SubtitleView(context)
        subtitlesView.setPlayer(player)

        val fullscreenHandler: FullscreenHandler
        if (fullscreenConfig.enable) {
            // Setting up the fullscreen feature
            fullscreenHandler = StreamFullscreenHandler(
                playerView,
                activity,
                fullscreenConfig
            )
            playerView.setFullscreenHandler(fullscreenHandler)
        }
        // Setting up the PiP feature
        if (fullscreenConfig.enable) {
            val pipHandler = PiPHandler(context.getActivity()!!, playerView)
            playerView.setPictureInPictureHandler(pipHandler)

            val pipExitHandler = object : PiPExitListener {
                override fun onPiPExit() {
                    pipHandler.exitPictureInPicture()
                }

                override fun isInPiPMode(): Boolean {
                    return pipHandler.isPictureInPicture
                }
            }
            this.pipExitHandler = pipExitHandler
            StreamsProvider.getInstance().pipChangesObserver.addListener(pipExitHandler)
        }

        if (state == BitmovinStreamState.INITIALIZING)
            state = BitmovinStreamState.WAITING_FOR_PLAYER
        else if (state == BitmovinStreamState.WAITING_FOR_VIEW) {
            state = BitmovinStreamState.DISPLAYING
            streamEventListener?.onStreamReady(player, playerView)
        }
    }

    fun dispose() {
        player?.destroy()
        pipExitHandler?.let { StreamsProvider.getInstance().pipChangesObserver.removeListener(it) }
        playerView?.let {
            it.setFullscreenHandler(null)
            it.setPictureInPictureHandler(null)
        }
        StreamsProvider.appContext.let {
            File(it.filesDir, "custom_css_${usid}.css")
                .takeIf { file -> file.exists() }?.delete()
        }
        StreamsProvider.getInstance().removeStream(usid)
        logger.i("Disposed")
    }
}

private fun Player.handleAttributes(
    autoPlay: Boolean,
    muted: Boolean,
    loop: Boolean,
    start: Double,
) {
    if (autoPlay)
        this.play()
    if (muted)
        this.mute()

    if (start > 0)
        this.seek(start)

    if (loop) {
        // Impl detail : we do not use the PlayerEvent.PlaybackFinished event because it triggers the ui visibility which seems undesirable
        this.on(PlayerEvent.TimeChanged::class.java) {
            // Delay action
            val player = this@handleAttributes
            var scheduledSeek = false
            if (player.currentTime > player.duration - 0.4 && !scheduledSeek) {
                scheduledSeek = true
                CoroutineScope(Dispatchers.IO).launch {
                    // Limit : If the video is paused at the end, it will be restarted anyway, but that is not a big deal since it's a really short window anyway
                    // 0.1 seems to be sufficient to never ever trigger the ui while not being noticeable by the user
                    val waitingTime =
                        ((player.duration - player.currentTime - 0.1) * 1000).toLong()
                    if (waitingTime > 0)
                        sleep(waitingTime)
                    scheduledSeek = false
                    player.seek(0.00)
                }
            }
        }
    }
}


internal enum class BitmovinStreamState {
    FETCHING,
    INITIALIZING,
    WAITING_FOR_VIEW,
    WAITING_FOR_PLAYER,
    DISPLAYING,
    DISPLAYING_ERROR,
}

enum class StreamError(private var message: String) {
    NO_INTERNET("No internet connection"),
    UNAUTHORIZED("Unauthorized access to stream. This stream may require a token."),
    FORBIDDEN_ACCESS("Forbidden access to stream. This stream may require a token."),
    STREAM_NOT_FOUND("Stream not found. Please check that the streamId is correct."),
    INTERNAL_SERVER_ERROR("Internal server error. Please try again later."),
    SERVICE_UNAVAILABLE("Service unavailable. Please try again later."),
    SOURCE_ERROR("Error while loading the source."),
    UNKNOWN_FETCHING_ERROR("An unknown error occurred during FETCHING."),
    UNKNOWN_ERROR("An unknown error occurred.");

    @Override
    override fun toString(): String {
        return message
    }

    companion object {
        fun fromHttpCode(httpCode: Int): StreamError {
            return when (httpCode) {
                0 -> NO_INTERNET
                401 -> UNAUTHORIZED
                403 -> FORBIDDEN_ACCESS
                404 -> STREAM_NOT_FOUND
                500 -> INTERNAL_SERVER_ERROR
                503 -> SERVICE_UNAVAILABLE
                else -> UNKNOWN_FETCHING_ERROR.apply { message = "Error $httpCode : $message" }
            }
        }
    }
}

internal class Logger(private val id: String, private val allLogs: Boolean) {

    fun i(message: String) {
        i(Tag.STREAM, message)
    }

    fun i(tag: String, message: String) {
        if (allLogs)
            Log.i(tag, "[$id] $message")
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (throwable == null)
            Log.e(Tag.STREAM, "[$id] $message")
        else
            Log.e(Tag.STREAM, "[$id] $message", throwable)
    }

    fun w(message: String) {
        Log.w(Tag.STREAM, "[$id] $message")
    }

    fun d(message: String) {
        if (allLogs)
            Log.d(Tag.STREAM, "[$id] $message")
    }

    fun <T> recordDuration(sectionName: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        return block().also {
            val duration = System.currentTimeMillis() - startTime
            performance(sectionName, duration)
        }
    }

    fun performance(sectionName: String, duration: Long) {
        i(Tag.PERF, "$sectionName took $duration ms")
    }

}

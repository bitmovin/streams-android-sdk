package com.bitmovin.streams

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import com.bitmovin.player.PlayerView
import com.bitmovin.player.SubtitleView
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.event.*
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.player.api.ui.FullscreenHandler
import com.bitmovin.player.api.ui.PictureInPictureHandler
import com.bitmovin.streams.config.BitmovinStreamEventListener
import com.bitmovin.streams.config.FullscreenConfig
import com.bitmovin.streams.config.StyleConfigStream
import com.bitmovin.streams.fullscreenmode.StreamFullscreenHandler
import com.bitmovin.streams.pipmode.PiPChangesObserver
import com.bitmovin.streams.pipmode.PiPExitListener
import com.bitmovin.streams.pipmode.PiPHandler
import com.bitmovin.streams.streamsjson.StreamConfigData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal class Stream(private val usid: String) {
    // These values are all set as mutableStateOf to trigger recompositions when they change
    var streamConfigData by mutableStateOf<StreamConfigData?>(null)
    var streamResponseError by mutableIntStateOf(0)
    var state by mutableStateOf(BitmovinStreamState.FETCHING)
    var pipHandler: PictureInPictureHandler? = null
    var fullscreenHandler: FullscreenHandler? = null
    var playerView by mutableStateOf<PlayerView?>(null)
    var player: Player? = null
    var streamEventListener: BitmovinStreamEventListener? = null
    var pipExitHandler: PiPExitListener? = null

    companion object {
        val pipChangesObserver = PiPChangesObserver()
    }

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
        state = BitmovinStreamState.FETCHING
        this.streamEventListener = bitmovinStreamEventListener
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val streamConfigDataResp = getStreamConfigData(streamId, jwToken)
                this@Stream.streamResponseError = streamConfigDataResp.responseHttpCode
                if (streamResponseError == 200) {
                    this@Stream.streamConfigData = streamConfigDataResp.streamConfigData
                    withContext(Dispatchers.Main) {
                        initializeStream(
                            context,
                            lifecycleOwner,
                            streamConfigData!!,
                            autoPlay,
                            loop,
                            muted,
                            start,
                            poster,
                            subtitles,
                            fullscreenConfig,
                            enableAds,
                            styleConfigStream
                        )
                    }
                } else {
                    error(streamId)
                }
            } catch (e: Exception) {
                error(streamId)
            }
        }
    }

    private fun error(streamId: String, msg: String? = null) {
        Log.e(
            Tag.Stream,
            "[$streamId] $streamResponseError : ${msg ?: getErrorMessage(streamResponseError)}"
        )
        streamEventListener?.onStreamError(
            streamResponseError,
            getErrorMessage(streamResponseError)
        )
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
        pipChangesObserver.let {
            it.context = context
            lifecycleOwner.lifecycle.addObserver(it)
        }

        val player = initializePlayerRelated(
            context,
            lifecycleOwner,
            streamConfig,
            autoPlay,
            loop,
            muted,
            start,
            enableAds,
            poster,
            subtitles
        )
        initializeViewRelated(
            context,
            player,
            lifecycleOwner,
            streamConfig,
            styleConfigStream,
            fullscreenConfig
        )
    }

    private fun initializePlayerRelated(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        streamConfig: StreamConfigData,
        autoPlay: Boolean,
        loop: Boolean,
        muted: Boolean,
        start: Double,
        enableAds: Boolean,
        poster: String?,
        subtitles: List<SubtitleTrack>
    ): Player {
        pipChangesObserver.let {
            it.context = context
            lifecycleOwner.lifecycle.addObserver(it)
        }

        // 1. Initializing the player
        val player = createPlayer(streamConfig, context, enableAds)
        this.player = player
        player.on(PlayerEvent.Ready::class.java) {
            if (state == BitmovinStreamState.INITIALIZING) {
                state = BitmovinStreamState.WAITING_FOR_VIEW
            } else if (state == BitmovinStreamState.WAITING_FOR_PLAYER) {
                state = BitmovinStreamState.DISPLAYING
                streamEventListener?.onStreamReady(player, playerView!!)
            }
        }

        // Warning: Running this block in the IO dispatcher could results in a crash (RuntimeException) if the component disappears before the end (because of the disposal effects)
        // This is also a lot faster on the main thread. However, it's a huge tradeoff because it's blocking the UI thread. No choice since we can't handle a RuntimeException.
        recordDuration("Loading Source") {
            // 2. Loading the stream source
            val streamSource =
                createSource(
                    streamConfig,
                    customPosterSource = poster,
                    subtitlesSources = subtitles
                )
            player.load(streamSource)


            // 3. Handling properties
            // Warning: The stream source has to be loaded before setting the properties. This is why we do it here.
            // PlayerEvent.Ready event not be called before the source is loaded.
            player.handleAttributes(autoPlay, muted, loop, start)
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

        // 4. Setting up Views

        // Setting up the player view
        playerView = createPlayerView(context, player, streamConfig, styleConfigStream, usid)
        val playerView = playerView!!
        // Adding the playerView to the lifecycle
        lifecycleOwner.lifecycle.addObserver(LifeCycleRedirectForPlayer(playerView))
        // Setting up the subtitles view
        val subtitlesView = SubtitleView(context)
        subtitlesView.setPlayer(player)


        if (state == BitmovinStreamState.INITIALIZING)
            state = BitmovinStreamState.WAITING_FOR_PLAYER
        else if (state == BitmovinStreamState.WAITING_FOR_VIEW) {
            state = BitmovinStreamState.DISPLAYING
            streamEventListener?.onStreamReady(player, playerView)
        }

        // 5. Initializing handlers

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
            pipHandler = PiPHandler(context.getActivity()!!, playerView)
            playerView.setPictureInPictureHandler(pipHandler)

            val pipExitHandler = object : PiPExitListener {
                override fun onPiPExit() {
                    this@Stream.pipHandler?.exitPictureInPicture()
                }

                override fun isInPiPMode(): Boolean {
                    return this@Stream.pipHandler?.isPictureInPicture ?: false
                }
            }
            this.pipExitHandler = pipExitHandler
            pipChangesObserver.addListener(pipExitHandler)
        }

    }

    fun dispose() {
        player?.destroy()
        pipExitHandler?.let { pipChangesObserver.removeListener(it) }
        playerView?.let {
            it.setFullscreenHandler(null)
            it.setPictureInPictureHandler(null)
        }
        StreamsProvider.appContext.let {
            File(it.filesDir, "custom_css_${usid}.css")
                .takeIf { file -> file.exists() }?.delete()
        }
        StreamsProvider.getInstance().removeStream(usid)
        Log.i(Tag.Stream, "[$usid] Stream disposed")
    }


    @Deprecated("How fetching method, not useful anymore")
    private fun fetchStreamConfig(
        streamId: String,
        jwToken: String?,
        bitmovinStreamEventListener: BitmovinStreamEventListener?
    ) {
        this.streamEventListener = bitmovinStreamEventListener
        // Coroutine IO to fetch the stream config data IO
        CoroutineScope(Dispatchers.IO).launch {
            // Fetch the stream config data
            try {
                val streamConfigDataResp = getStreamConfigData(streamId, jwToken)
                streamResponseError = streamConfigDataResp.responseHttpCode
                when (streamResponseError) {
                    200 -> {
                        streamConfigData = streamConfigDataResp.streamConfigData
                        state = BitmovinStreamState.INITIALIZING
                    }

                    else -> {
                        Log.e(
                            Tag.Stream,
                            streamResponseError.toString() + "Error fetching stream [$streamId] config data."
                        )
                        streamEventListener?.onStreamError(
                            streamResponseError,
                            getErrorMessage(streamResponseError)
                        )
                        state = BitmovinStreamState.DISPLAYING_ERROR
                    }
                }
            } catch (e: Exception) {
                Log.e(Tag.Stream, "No Internet Connection", e)
                state = BitmovinStreamState.DISPLAYING_ERROR
                streamEventListener?.onStreamError(
                    streamResponseError,
                    getErrorMessage(streamResponseError)
                )
            }
        }
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

    // Prototype for an autoplay feature, but it's not looking great for now
    if (loop) {
        // Impl detail : we do not use the PlayerEvent.PlaybackFinished event because it triggers the ui visibility which seems undesirable
        this.on(PlayerEvent.TimeChanged::class.java) {
            // Delay action
            val player = this@handleAttributes
            var scheduledSeek = false
            if (player.currentTime > player.duration - 0.3 && !scheduledSeek) {
                object : Thread() {
                    override fun run() {
                        scheduledSeek = true
                        // Limit : If the video is paused at the end, it will be restarted anyway, but that is not a big deal since it's a really short window anyway
                        // 0.05 seems to be sufficient to never ever trigger the ui but it might not be enough for all devices, need some testing
                        val waitingTime =
                            ((player.duration - player.currentTime - 0.8) * 1000).toLong()
                        if (waitingTime > 0)
                            Thread.sleep(waitingTime)
                        scheduledSeek = false
                        player.seek(0.00)
                    }
                }.start()
            }
        }
    }
}


enum class BitmovinStreamState {
    FETCHING,
    INITIALIZING,
    WAITING_FOR_VIEW,
    WAITING_FOR_PLAYER,
    DISPLAYING,
    DISPLAYING_ERROR,
}
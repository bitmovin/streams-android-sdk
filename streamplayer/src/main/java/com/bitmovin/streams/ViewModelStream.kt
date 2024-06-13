package com.bitmovin.streams

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch

internal class ViewModelStream : ViewModel() {
    // These values are all set as mutableStateOf to trigger recompositions when they change
    var streamConfigData by mutableStateOf<StreamConfigData?>(null)
    var streamResponseError by mutableIntStateOf(0)
    var state by mutableStateOf(BitmovinStreamState.FETCHING)
    var context by mutableStateOf<Context?>(null)
    var pipHandler: PictureInPictureHandler? = null
    var fullscreenHandler: FullscreenHandler? = null
    var immersiveFullScreen = mutableStateOf(true)
    var playerView by mutableStateOf<PlayerView?>(null)
    var player: Player? = null
    var streamEventListener: BitmovinStreamEventListener? = null

    companion object {
        val pipChangesObserver = PiPChangesObserver()
    }

    fun fetchStreamConfigData(streamId: String, jwToken: String?, bitmovinStreamEventListener: BitmovinStreamEventListener?) {
        Log.d("BitmovinStream", "Fetching the Stream Data of $streamId")
        this.streamEventListener = bitmovinStreamEventListener
        viewModelScope.launch {
            // Fetch the stream config data
            try {
                val streamConfigDataResp = getStreamConfigData(streamId, jwToken)
                streamResponseError = streamConfigDataResp.responseHttpCode
                when (streamResponseError) {
                    200 -> {
                        streamConfigData = streamConfigDataResp.streamConfigData
                        updateState(Action.FETCHING_DONE)
                        Log.d("BitmovinStream", "Stream Config Data of $streamId fetched")
                    }

                    else -> {
                        updateState(Action.STREAM_ERROR)
                    }
                }
            } catch (e: Exception) {
                updateState(Action.STREAM_ERROR)
            }
        }
    }

    /*
        * Initialize the player with the stream config data and Stream Player attributes
        * Should be called after the stream config data has been fetched.
     */
    fun initializePlayer(
        context: Context,
        streamId: String,
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
        Log.d("BitmovinStream", "Initializing the BitmovinStream of $streamId")
        pipChangesObserver.let {
            it.context = context
            lifecycleOwner.lifecycle.addObserver(it)
        }
        this.context = context
        val activity = context.getActivity()

        // 1. Initializing the player
        Log.v("BitmovinStream", "1 - Initializing the player")
        val player = createPlayer(streamConfig, context, enableAds)
        this.player = player
        player.on(PlayerEvent.Ready::class.java) {
            updateState(Action.PLAYER_READY)
        }


        // 2. Loading the stream source
        Log.v("BitmovinStream", "2 - Loading the stream source")
        val streamSource =
            createSource(streamConfig, customPosterSource = poster, subtitlesSources = subtitles)
        player.load(streamSource)

        // 3. Handling properties
        Log.v("BitmovinStream", "3 - Handling properties")
        player.handleAttributes(autoPlay, muted, loop, start)
        this.immersiveFullScreen.value = fullscreenConfig.immersive

        // 4. Setting up Views
        Log.v("BitmovinStream", "4 - Setting up views")

        // Setting up the player view
        playerView = createPlayerView(context, player, streamId, streamConfig, styleConfigStream)
        val playerView = playerView!!
        // Adding the playerView to the lifecycle
        lifecycleOwner.lifecycle.addObserver(LifeCycleRedirectForPlayer(playerView))
        // Setting up the subtitles view
        val subtitlesView = SubtitleView(context)
        subtitlesView.setPlayer(player)

        // 5. Initializing handlers
        Log.v("BitmovinStream", "5 - Initializing handlers")

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
            pipHandler = PiPHandler(context.getActivity()!!, playerView, this.immersiveFullScreen)
            playerView.setPictureInPictureHandler(pipHandler)

            val pipExitHandler = object : PiPExitListener {
                override fun onPiPExit() {
                    this@ViewModelStream.pipHandler?.exitPictureInPicture()
                }

                override fun isInPiPMode(): Boolean {
                    return this@ViewModelStream.pipHandler?.isPictureInPicture ?: false
                }
            }
            pipChangesObserver.addListener(pipExitHandler)
        }

        updateState(Action.PLAYER_VIEW_READY)
        Log.d("BitmovinStream", "BitmovinStream of $streamId initialized")
    }

    private fun updateState(action: Action) {
        when (action) {
            Action.FETCHING_DONE -> {
                state = BitmovinStreamState.INITIALIZING
            }
            Action.PLAYER_VIEW_READY -> {
                if (state == BitmovinStreamState.INITIALIZING) {
                    state = BitmovinStreamState.WAITING_FOR_PLAYER
                    streamEventListener?.onPlayerViewReady(playerView!!)
                    Log.d("BitmovinStream", "PlayerView ready")
                }
                else if (state == BitmovinStreamState.WAITING_FOR_VIEW)
                {
                    state = BitmovinStreamState.DISPLAYING
                    streamEventListener?.onStreamReady(player!!, playerView!!)
                    Log.d("BitmovinStream", "Stream ready")
                }
            }
            Action.PLAYER_READY -> {
                if (state == BitmovinStreamState.INITIALIZING) {
                    state = BitmovinStreamState.WAITING_FOR_VIEW
                    streamEventListener?.onPlayerReady(player!!)
                    Log.d("BitmovinStream", "Player ready")
                }
                else if (state == BitmovinStreamState.WAITING_FOR_PLAYER)
                {
                    state = BitmovinStreamState.DISPLAYING
                    streamEventListener?.onStreamReady(player!!, playerView!!)
                    Log.d("BitmovinStream", "Stream ready")
                }
            }
            Action.STREAM_ERROR -> {
                streamEventListener?.onStreamError(streamResponseError, "Error fetching or initializing the player")
                state = BitmovinStreamState.DISPLAYING_ERROR
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

enum class Action {
    FETCHING_DONE,
    PLAYER_VIEW_READY,
    PLAYER_READY,
    STREAM_ERROR,
}
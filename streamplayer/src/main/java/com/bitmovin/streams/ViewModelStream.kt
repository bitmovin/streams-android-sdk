package com.bitmovin.streams

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    fun fetchStreamConfigData(streamId: String, jwToken: String?, bitmovinStreamEventListener: BitmovinStreamEventListener?) {
        this.streamEventListener = bitmovinStreamEventListener
        viewModelScope.launch {
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
                            "StreamsPlayer",
                            streamResponseError.toString() + "Error fetching stream config data."
                        )
                        streamEventListener?.onStreamError(streamResponseError, getErrorMessage(streamResponseError))
                        state = BitmovinStreamState.DISPLAYING_ERROR
                    }
                }
            } catch (e: Exception) {
                Log.e("StreamsPlayer", "No Internet Connection", e)
                state = BitmovinStreamState.DISPLAYING_ERROR
                streamEventListener?.onStreamError(streamResponseError, getErrorMessage(streamResponseError))
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
        this.context = context
        val activity = context.getActivity()

        // 1. Initializing the player
        val player = createPlayer(streamConfig, context, enableAds)
        this.player = player
        player.on(PlayerEvent.Ready::class.java) {
            streamEventListener?.onPlayerReady(player)
            if (state == BitmovinStreamState.INITIALIZING) {
                state = BitmovinStreamState.WAITING_FOR_VIEW
            } else if (state == BitmovinStreamState.WAITING_FOR_PLAYER) {
                state = BitmovinStreamState.DISPLAYING
                streamEventListener?.onStreamReady(player, playerView!!)
            }
        }


        // 2. Loading the stream source
        val streamSource =
            createSource(streamConfig, customPosterSource = poster, subtitlesSources = subtitles)
        player.load(streamSource)

        // 3. Handling properties
        player.handleAttributes(autoPlay, muted, loop, start)
        this.immersiveFullScreen.value = fullscreenConfig.immersive

        // 4. Setting up Views

        // Setting up the player view
        playerView = createPlayerView(context, player, streamId, streamConfig, styleConfigStream)
        val playerView = playerView!!
        // Adding the playerView to the lifecycle
        lifecycleOwner.lifecycle.addObserver(LifeCycleRedirectForPlayer(playerView))
        // Setting up the subtitles view
        val subtitlesView = SubtitleView(context)
        subtitlesView.setPlayer(player)

        // 5. Initializing handlers

        // Setting up the fullscreen feature
        fullscreenHandler = StreamFullscreenHandler(
            playerView,
            activity,
            fullscreenConfig
        )
        playerView.setFullscreenHandler(fullscreenHandler)

        // Setting up the PiP feature
        pipHandler = PiPHandler(context.getActivity()!!, playerView, this.immersiveFullScreen)
        playerView.setPictureInPictureHandler(pipHandler)
        streamEventListener?.onPlayerViewReady(playerView)
        if (state == BitmovinStreamState.INITIALIZING)
            state = BitmovinStreamState.WAITING_FOR_PLAYER
        else if (state == BitmovinStreamState.WAITING_FOR_VIEW) {
            state = BitmovinStreamState.DISPLAYING
            streamEventListener?.onStreamReady(player, playerView)
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
            if (player.currentTime > player.duration - 0.25 && !scheduledSeek) {
                object : Thread() {
                    override fun run() {
                        scheduledSeek = true
                        // Limit : If the video is paused at the end, it will be restarted anyway, but that is not a big deal since it's a really short window anyway
                        // 0.05 seems to be sufficient to never ever trigger the ui but it might not be enough for all devices, need some testing
                        val waitingTime =
                            ((player.duration - player.currentTime - 0.05) * 1000).toLong()
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
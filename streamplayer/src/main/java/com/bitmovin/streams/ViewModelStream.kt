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
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.player.api.ui.PictureInPictureHandler
import com.bitmovin.streams.pipmode.PiPHandler
import com.bitmovin.streams.streamsjson.StreamConfigData
import kotlinx.coroutines.launch

internal class ViewModelStream : ViewModel() {
    // These values are all set as mutableStateOf to trigger recompositions when they change
    val isFullScreen = mutableStateOf(false)
    var streamConfigData by mutableStateOf<StreamConfigData?>(null)
    var streamResponseError by mutableIntStateOf(0)
    var state by mutableStateOf(BitmovinStreamState.FETCHING)
    var context by mutableStateOf<Context?>(null)
    var pipHandler : PictureInPictureHandler? = null
    var fullscreenHandler : DefaultStreamFullscreenHandler? = null
    var immersiveFullScreen by mutableStateOf(true)
    var playerView by mutableStateOf<PlayerView?>(null)
    var player : Player? = null
    var subtitlesView by mutableStateOf<SubtitleView?>(null)

    fun fetchStreamConfigData(streamId: String, jwToken: String?) {
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
                    Log.e("StreamsPlayer", streamResponseError.toString() + "Error fetching stream config data.")
                    state = BitmovinStreamState.DISPLAYING_ERROR
                }
            }
            } catch (e: Exception) {
                Log.e("StreamsPlayer", "No Internet Connection", e)
                state = BitmovinStreamState.DISPLAYING_ERROR
            }
        }
    }

    fun initializePlayer( context: Context, lifecycleOwner: LifecycleOwner, streamEventListener: BitmovinStreamEventListener?, streamConfig: StreamConfigData, autoPlay: Boolean, muted: Boolean, start: Double, poster: String?, subtitles: List<SubtitleTrack>, immersiveFullScreen: Boolean) {
        this.context = context
        val activity = context.getActivity()
        player = createPlayer(streamConfig, context)
        val player = player!! // Garanteed to be createPlayer

        // Loading the stream source
        val streamSource = createSource(streamConfig, customPosterSource = poster, subtitlesSources = subtitles)
        player.load(streamSource)

        // Handling properties
        this.immersiveFullScreen = immersiveFullScreen
        if (autoPlay)
            player.play()
        if (muted)
            player.mute()

        player.seek(start)


        streamEventListener?.onPlayerReady(player)


        // Setting up the player view
        playerView = createPlayerView(context, player)
        val playerView = playerView!!
        lifecycleOwner.lifecycle.addObserver(LifeCycleRedirectForPlayer(playerView))


        // Setting up the fullscreen feature
        fullscreenHandler = DefaultStreamFullscreenHandler(playerView, activity, isFullScreen)
        playerView.setFullscreenHandler(fullscreenHandler)

        // Setting up the PiP feature
        pipHandler = PiPHandler(this, context.getActivity()!!, player)
        playerView.setPictureInPictureHandler(pipHandler)

        // Setting up the subtitles view
        subtitlesView = SubtitleView(context)
        subtitlesView!!.setPlayer(player)

        streamEventListener?.onPlayerViewReady(playerView)

        // Setup done, we can display the player
        state = BitmovinStreamState.DISPLAYING
    }

}


enum class BitmovinStreamState {
    FETCHING,
    INITIALIZING,
    DISPLAYING,
    DISPLAYING_ERROR,
}


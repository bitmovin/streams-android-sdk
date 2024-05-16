package com.bitmovin.streams

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitmovin.player.PlayerView
import com.bitmovin.player.SubtitleView
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.player.api.ui.FullscreenHandler
import com.bitmovin.streams.streamsjson.StreamConfigData
import kotlinx.coroutines.launch

class ViewModelStream : ViewModel() {
    val isFullScreen = mutableStateOf(false)
    var streamConfigData by mutableStateOf<StreamConfigData?>(null)
    var streamResponseError by mutableIntStateOf(0)
    var state by mutableStateOf(StreamDataBridgeState.FETCHING)
    var context by mutableStateOf<Context?>(null)

    var playerView by mutableStateOf<PlayerView?>(null)
    var subtitlesView by mutableStateOf<SubtitleView?>(null)

    fun fetchStreamConfigData(streamId: String, jwToken: String?) {
        viewModelScope.launch {
            // Fetch the stream config data
            val streamConfigDataResp = getStreamConfigData(streamId, jwToken)
            streamResponseError = streamConfigDataResp.responseHttpCode
            when (streamResponseError) {
                200 -> {
                    streamConfigData = streamConfigDataResp.streamConfigData
                    state = StreamDataBridgeState.INITIALIZING
                }
                401 -> {
                    Log.e("StreamsPlayer", "Unauthorized access to stream\nThis stream may be private or require a token.")
                    state = StreamDataBridgeState.DISPLAYING_ERROR
                }
                403 -> {
                    Log.e("StreamsPlayer", "Forbidden access to stream\nThe domain may not be allowed to access the stream or the token you provided may be invalid.")
                    state = StreamDataBridgeState.DISPLAYING_ERROR
                }
                else -> {
                    Log.e("StreamsPlayer", streamResponseError.toString() + "Error fetching stream config data.")
                    state = StreamDataBridgeState.DISPLAYING_ERROR
                }
            }
        }
    }

    fun initializePlayer(context: Context, streamConfig: StreamConfigData, autoPlay: Boolean, muted: Boolean, start: Double, poster: String?, subtitles: List<SubtitleTrack>) {
        val player = createPlayer(streamConfig, context)
        val streamSource = createSource(streamConfig, customPosterSource = poster, subtitlesSources = subtitles)
        this.context = context

        // Loading the stream source
        player.load(streamSource)

        // Handling properties
        if (autoPlay)
            player.play()
        if (muted)
            player.mute()

        player.seek(start)

        // UI
        val fullscreenHandler = FullScreenHandler(isFullScreen, context)
        subtitlesView = SubtitleView(context)
        subtitlesView!!.setPlayer(player)

        playerView = createPlayerView(context, player)
        playerView!!.setFullscreenHandler(fullscreenHandler)

        state = StreamDataBridgeState.DISPLAYING
    }
}

enum class StreamDataBridgeState {
    FETCHING,
    INITIALIZING,
    DISPLAYING,
    DISPLAYING_ERROR,
}


package com.bitmovin.streams

import com.bitmovin.streams.pipmode.PiPChangesObserver
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bitmovin.player.api.media.subtitle.SubtitleTrack
import com.bitmovin.streams.pipmode.PiPExitListener
import java.util.UUID
import kotlin.reflect.KProperty

const val MAX_FETCH_ATTEMPTS_STREAMS_CONFIG = 3

/**
 * Bitmovin Streams Player Component.
 *
 * @param streamId The id of the stream to be played.
 *
 * @param jwToken The token to be used for authentication.
 * @param autoPlay Whether the player should start playing automatically.
 * @param muted Whether the player should be muted.
 * @param poster The poster image to be displayed before the player starts.
 * @param start The time in seconds at which the player should start playing.
 */
@Composable
fun BitmovinStream(
    streamId : String,
    modifier : Modifier = Modifier,
    jwToken : String? = null,
    autoPlay : Boolean = false,
    muted : Boolean = false,
    poster : String? = null,
    start : Double = 0.0,
    subtitles : List<SubtitleTrack> = emptyList(),
    immersiveFullScreen : Boolean = true
) {
    Log.d("StreamsPlayer", "StreamsPlayer called")
    val context = LocalContext.current

    // Make the ViewModel unique for each instance of the Streams Player
    // The UPID (Unique Player ID) is maintained through recompositions to keep the ViewModel alive and used.
    // We do not use the streamId to allow to user to have multiple players with the same streamId.
    val upid: String by rememberSaveable { UUID.randomUUID().toString() }
    val viewModel: ViewModelStream = viewModel(key = upid)

    // There is only one PiPManager for the whole application
    val pipManager: PiPChangesObserver = viewModel()
    LocalLifecycleOwner.current.lifecycle.addObserver(pipManager)
    pipManager.addListener(object : PiPExitListener {
        override fun onPiPExit() {
            Log.d("StreamsPlayer", "onPiPExit called")
            viewModel.pipHandler?.exitPictureInPicture()
        }

        override fun isInPiPMode(): Boolean {
            return viewModel.pipHandler?.isPictureInPicture ?: false
        }
    })

    when (viewModel.state) {
        BitmovinStreamState.DISPLAYING -> {
            val playerView = viewModel.playerView!!
            val subtitlesView = viewModel.subtitlesView!!

            if (viewModel.isFullScreen.value) {
                FullScreen(
                    onDismissRequest = { viewModel.isFullScreen.value = false },
                    immersive = viewModel.immersiveFullScreen
                ) {
                    StreamVideoPlayer(playerView = playerView, subtitleView = subtitlesView)
                }
            } else {
                StreamVideoPlayer(playerView = playerView, subtitleView = subtitlesView, modifier = modifier)
            }
        }

        // One-time actions for fetching and initializing the player
        BitmovinStreamState.FETCHING -> {
            LaunchedEffect(Unit) {
                viewModel.fetchStreamConfigData(streamId, jwToken)
            }
            TextVideoPlayerFiller("Fetching stream data", modifier)
        }
        BitmovinStreamState.INITIALIZING -> {
            LaunchedEffect(Unit) {
                viewModel.initializePlayer(context, viewModel.streamConfigData!!, autoPlay, muted, start, poster, subtitles, immersiveFullScreen)
            }
            TextVideoPlayerFiller("Initializing player", modifier)
        }
        BitmovinStreamState.DISPLAYING_ERROR -> {
            ErrorHandling(error = viewModel.streamResponseError, modifier)
        }
    }
}

private operator fun String.getValue(nothing: Nothing?, property: KProperty<*>): String {
    return this
}

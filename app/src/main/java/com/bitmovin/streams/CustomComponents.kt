package com.bitmovin.streams

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bitmovin.player.PlayerView
import com.bitmovin.player.SubtitleView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.PlayerConfig
import com.bitmovin.player.ui.DefaultPictureInPictureHandler
import kotlin.collections.MutableSet as MutableSet1


/**
 * Player wrapped into a compose element
 */
@Composable
fun StreamVideoPlayer(playerView: PlayerView, subtitleView: SubtitleView, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        AndroidView(factory = { _ -> playerView.apply { if (parent != null) this.removeFromParent() } })
        AndroidView(factory = { _ -> subtitleView.apply { if (parent != null) this.removeFromParent() } })
    }
}

/**
 * Make the content fill the screen
 *
 * @param onDismissRequest: Callback to dismiss the dialog
 * @param immersive: true to make the content immersive by hiding the Android UI and being Borderless
 * @param content: The content to display
 *
 * TODO: More flexible parameters to allow for more use-cases, but it's doing the job well for the video player
 */
@Composable
fun FullScreen(
    onDismissRequest: () -> Unit,
    immersive: Boolean = true,
    content: @Composable () -> Unit
) {
    var properties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false
    )
    if (immersive) {
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = false
        )
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        content = {
            if (immersive) {
                val activityWindow = getActivityWindow()
                val dialogWindow = getDialogWindow()
                val parentView = LocalView.current.parent as View

                SideEffect {
                    if (activityWindow != null && dialogWindow != null) {
                        val attributes = WindowManager.LayoutParams().apply {
                            copyFrom(activityWindow.attributes)
                            type = dialogWindow.attributes.type
                        }
                        dialogWindow.attributes = attributes
                        parentView.layoutParams = FrameLayout.LayoutParams(
                            activityWindow.decorView.width,
                            activityWindow.decorView.height
                        )
                    }
                }

                val context = LocalContext.current
                val window = getDialogWindow()

                DisposableEffect(Unit) {
                    val originalStatusBarColor = window?.statusBarColor
                    window?.statusBarColor = Color.Transparent.toArgb()
                    window?.decorView?.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                    onDispose {
                        if (originalStatusBarColor != null) {
                            window.statusBarColor = originalStatusBarColor
                        }
                        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    }
                }

                DisposableEffect(Unit) {
                    window?.decorView?.systemUiVisibility =
                        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

                    onDispose {
                        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                    content()
                }
            } else {
                content()
            }
        }
    )
}

/**
 * Video player replacement element for handling errors and waiting time.
 * @param text The text to be displayed.
 */
@Composable
fun TextVideoPlayerFiller(text : String, modifier: Modifier = Modifier) {
    //TODO: Match the native errors better
    val player = Player(LocalContext.current, PlayerConfig(key = "FILLER"))
    val playerView = PlayerView(LocalContext.current, player)
    playerView.alpha = 0.0f
    Box(
        modifier = Modifier.background(color = Color.Black)
    )
    {
        AndroidView(factory =
        { _ -> playerView.apply {
            if (parent != null)
                this.removeFromParent() }
        }, modifier = modifier)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp),
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
@Composable
fun ErrorHandling(error: Int, modifier: Modifier = Modifier) {
    val message =
        when (error) {
            401 -> "Unauthorized access to stream\nThis stream may require a token."
            403 -> "Forbidden access to stream\nThe domain may not be allowed to access the stream or the token you provided may be invalid."
            404 -> "Stream not found\nThe stream you are trying to access does not exist."
            500 -> "Internal server error\nThe server encountered an error while processing your request."
            503 -> "Service unavailable\nPlease try again in few minutes."
            else -> "An error occurred while fetching the stream data."
        }
    TextVideoPlayerFiller("Error $error\n$message", modifier)
}
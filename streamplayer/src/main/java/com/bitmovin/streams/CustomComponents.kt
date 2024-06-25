package com.bitmovin.streams

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bitmovin.player.PlayerView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Player wrapped into a compose element
 */
@Composable
internal fun StreamVideoPlayer(playerView: PlayerView, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        AndroidView(factory = { _ -> playerView.apply { this.removeFromParent() } })
    }
}

/**
 * Make the content fill the screen
 *
 * @param onDismissRequest: Callback to dismiss the dialog
 * @param isImmersive: true to make the content immersive by hiding the Android UI and being Borderless
 * @param content: The content to display
 *
 */
@Suppress("DEPRECATION")
@Composable
internal fun FullScreen(
    onDismissRequest: () -> Unit,
    isImmersive: Boolean = true,
    content: @Composable () -> Unit
) {
    val properties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = isImmersive,
        decorFitsSystemWindows = false
    )
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        content = {
            val dialogWindow = getDialogWindow()
            if (isImmersive) {
                val activityWindow = getActivityWindow()
                val parentView = LocalView.current.parent as View

                var width by remember {
                    mutableIntStateOf(
                        activityWindow?.decorView?.width ?: 0
                    )
                }
                var height by remember {
                    mutableIntStateOf(
                        activityWindow?.decorView?.height ?: 0
                    )
                }

                SideEffect {
                    if (activityWindow != null && dialogWindow != null) {
                        val attributes = WindowManager.LayoutParams().apply {
                            copyFrom(activityWindow.attributes)
                            type = dialogWindow.attributes.type
                        }
                        dialogWindow.attributes = attributes
                        parentView.layoutParams = FrameLayout.LayoutParams(
                            width,
                            height
                        )

                        /*
                         There is some unpredictable delay depending of the device that led to having the wrong width and height, especially while escaping from PiP mode and entering.
                         This is a workaround to get the right width and height during a 0.5 seconds period which restart itself whenever there's a change (which should be reasonable for most devices).
                         This method is not perfect and cause some short visual glitches, but it's better than having the wrong width and height.
                         NB : This issue is not present when the dialog is not immersive.
                        */
                        CoroutineScope(Dispatchers.IO).launch {
                            for (i in 0..10) {
                                Thread.sleep(50)
                                if (activityWindow.decorView.width != width || activityWindow.decorView.height != height) {
                                    width = activityWindow.decorView.width
                                    height = activityWindow.decorView.height
                                    // Break it since the SideEffect will be called again anyway
                                    break
                                }
                            }
                        }
                    }
                }

                DisposableEffect(Unit) {
                    dialogWindow?.decorView?.systemUiVisibility =
                        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.KEEP_SCREEN_ON)

                    onDispose {
                        dialogWindow?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    }
                }

                key(width /* height is not required since both will be updated anyway if there is a change */) {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                        content()
                    }
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
internal fun TextVideoPlayerFiller(
    text: String,
    modifier: Modifier = Modifier,
    loadingEffect: Boolean = false
) {
    Box(
        modifier = modifier.background(color = Color.Black)
    )
    {
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
            if (loadingEffect)
                CircularLoadingAnimation(Modifier.padding(16.dp))
        }
    }
}

@Composable
internal fun ErrorHandling(streamError: StreamError, modifier: Modifier = Modifier) {
    TextVideoPlayerFiller(streamError.toString(), modifier)
}

@Composable
internal fun CircularLoadingAnimation(
    modifier: Modifier = Modifier,
    // blueish default
    circleColor: Color = Color(32, 172, 227),
    circleStrokeWidth: Float = 12f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing)
        ),
        label = "Arc's Starting point"
    )
    val angleSize by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 235f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Arc's Size (as angle)"
    )

    Canvas(modifier = modifier.size(30.dp)) {
        drawArc(
            color = circleColor,
            startAngle = angle,
            sweepAngle = angleSize,
            useCenter = false,
            style = Stroke(width = circleStrokeWidth)
        )
    }
}




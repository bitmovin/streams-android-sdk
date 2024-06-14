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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay


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
 * TODO: More flexible parameters to allow for more use-cases, but it's doing the job well for the video player
 */
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
    val orientation = LocalConfiguration.current.orientation
    // Recompose when orientation changes
    key(orientation) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = properties,
            content = {
                if (isImmersive) {
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

                    val window = getDialogWindow()

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

                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                        content()
                    }
                } else {
                    content()
                }
            }
        )
    }
}

/**
 * Video player replacement element for handling errors and waiting time.
 * @param text The text to be displayed.
 */
@Composable
internal fun TextVideoPlayerFiller(text : String, modifier: Modifier = Modifier, noiseEffect: Boolean = false, loadingEffect: Boolean = false) {
    // This is a hack to assert the same behavior as the PlayerView even when it don't exists to avoid breaking the layout of the users.
//    val player = Player(LocalContext.current, PlayerConfig(key = "__FILLER_KEY__"))
//    val playerView = PlayerView(LocalContext.current, player)
//    playerView.alpha = 0.0f
    Box(
        modifier = modifier.background(color = Color.Black)
    )
    {
        if (noiseEffect)
            NoiseEffect() // Seems to be a bit heavy so I just let this here for now
//        AndroidView(factory =
//        { _ -> playerView.apply {
//            if (parent != null)
//                this.removeFromParent() }
//        }, modifier = modifier)
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
internal fun ErrorHandling(error: Int, modifier: Modifier = Modifier) {
    var message = getErrorMessage(error)
    if (error != 0)
        message = "Error $error\n$message"
    TextVideoPlayerFiller(message, modifier, noiseEffect = false)
}

@Composable
internal fun PictureInPictureHandlerForStreams(viewModel: Stream) {
    // There is only one PiPManager for the whole application

    // Get local fragment if it exists
//    val pitiee = LocalContext.current.getActivity()
//    val pipManager: PiPChangesObserver = viewModel()
//
//    LocalLifecycleOwner.current.lifecycle.addObserver(pipManager) // /!\ It only matters the first time a BitmovinStream is created, afterwards it is ignored
//    DisposableEffect(Unit) {
//        val obj = object : PiPExitListener {
//            override fun onPiPExit() {
//                Log.d("StreamsPlayer", "onPiPExit called")
//                viewModel.pipHandler?.exitPictureInPicture()
//            }
//
//            override fun isInPiPMode(): Boolean {
//                return viewModel.pipHandler?.isPictureInPicture ?: false
//            }
//        }
//        pipManager.addListener(obj)
//
//        onDispose {
//            pipManager.removeListener(obj)
//        }
//    }

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















// LABORATORY
@Deprecated("Too heavy for now, need to be optimized or just not used.")
@Composable
fun NoiseEffect(
    modifier: Modifier = Modifier,
    noiseFactor: Float = 0.5f
) {
    Box(modifier = modifier.fillMaxSize()) {
        val noiseSeed = remember { mutableLongStateOf(0) }

        LaunchedEffect(noiseSeed) {
            while (true) {
                noiseSeed.longValue = (noiseSeed.longValue + 1) % 1000
                delay(75)
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawNoise(
                noiseValue = noiseFactor,
                noiseSeed = (noiseSeed.longValue),
                size = size
            )
        }
    }
}

internal fun DrawScope.drawNoise(
    noiseValue: Float,
    noiseSeed: Long,
    size: Size
) {
    val random = java.util.Random(noiseSeed)
    val pixelSize = 3.dp.toPx()
    val width = (size.width / pixelSize).toInt()
    val height = (size.height / pixelSize).toInt()

    for (x in 0 until width) {
        for (y in 0 until height) {
            val noiseThreshold = random.nextFloat()
            val color = if (noiseThreshold < noiseValue) Color.Black else Color.DarkGray
            // Too Heavy
            drawRect(
                color = color,
                topLeft = Offset(x * pixelSize, y * pixelSize),
                size = Size(pixelSize, pixelSize)
            )
        }
    }
}




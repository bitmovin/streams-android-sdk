package com.bitmovin.testapp

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.event.PlayerEvent
import com.bitmovin.streams.BitmovinStream
import com.bitmovin.streams.TestStreamsIds
import com.bitmovin.streams.config.BitmovinStreamConfig
import com.bitmovin.streams.config.BitmovinStreamEventListener
import com.bitmovin.streams.config.PlayerThemes
import com.bitmovin.streams.config.StyleConfigStream
import com.bitmovin.testapp.ui.theme.StreamsandroidsdkTheme
import kotlinx.coroutines.delay


class ScrollingExampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StreamsandroidsdkTheme {
                Column {
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
                    BitmovinShowcase()
                }

            }
        }
    }
}

@Composable
fun BitmovinShowcase() {
    val scrollState = rememberScrollState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxWidth()
            .padding(if (isLandscape) 144.dp else 10.dp, 0.dp)
    ) {
        Text(
            text = "Discovering the Bitmovin Stream Player Component",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(16.dp)
        )

        Text(
            text = "Using it",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp, 0.dp)
        )

        Text(
            text = """
                This demonstration showcases the capabilities of the Bitmovin Stream Player component in an Android app.
                To create a Stream Player, you should call the BitmovinStream composable function and pass the streamId as parameters from a ComposeView. That's all folks.
            """.trimIndent(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )

        FlipCard(
            "Basic Stream Player",
            R.drawable.code_snippet_1
        ) {

            // Simplest way to use the Bitmovin Stream Player component
            BitmovinStream(
                streamId = TestStreamsIds.SINTEL,
                styleConfig = PlayerThemes.BITMOVIN_DEFAULT_THEME
            )
        }

        Text(
            text = """
                Just like that, the component will setup itself with the stream's dashboard configuration properties.
                Warning : If your stream requires a token, you should pass it as a parameter to the BitmovinStream function.
            """.trimIndent(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = "Android Stream Player properties and capabilities",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp, 0.dp)
        )
        Text(
            text = """
                There are many properties and capabilities that you can use to customize the player dynamically.
                
                Here is a quick list of properties that you can use.
                
            """.trimIndent(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
        // Properties list
        // TODO: Document it properly, for now it's ust a chatGPT filler without necessary details
        val properties = listOf(
            "streamId" to "The id of the stream to be played.",
            "modifier" to "The modifier to be applied to the player. Default: Modifier",
            "jwToken" to "The token to be used for authentication if the stream is protected. Default: null",
            "autoPlay" to "Whether the player should start playing automatically. Default: false",
            "muted" to "Whether the player should be muted. Default: true",
            "poster" to "The poster image to be displayed before the player starts. Default: null",
            "start" to "The time in seconds at which the player should start playing. Default: 0.0",
            "subtitles" to "The list of subtitle tracks available for the stream. Default: emptyList()",
            "immersiveFullScreen" to "Whether the player should be in immersive full screen mode. Default: true",
            "bitmovinStreamEventListener" to "The listener for the player events. Default: null",
            "screenOrientationOnFullscreenEscape" to "The screen orientation to be set when the player exits full screen. Default behavior is to go back in the orientation preceding the full screen mode.",
            "enableAds" to "Whether ads should be enabled. Default: true",
            "styleConfig" to "The style configuration for the player. Default: StyleConfigStream()"
        )
        repeat(properties.size) {
            if (it == 0) {
                Text(
                    text = "Mandatory properties",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            PropertyCard(properties[it])
            if (it < properties.size - 1 && it > 0) {
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (it == 0) {
                Text(
                    text = "Optional properties",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = """
                Here are some examples of how you can take advantage of the different properties to sweet your own need.
            """.trimIndent(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )

        FlipCard(
            "Example 1 - Theme customization",
            R.drawable.code_snippet_1
        ) {

            BitmovinStream(
                streamId = TestStreamsIds.SQUARE_VIDEO,
                styleConfig = PlayerThemes.RED_EXAMPLE_THEME,
                autoPlay = true,
                muted = true,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        FlipCard(
            "Example 2 - Vertical Video",
            R.drawable.code_snippet_1,
            aspectRatio = 1f
        ) {

            BitmovinStream(
                streamId = TestStreamsIds.VERTICAL_VIDEO,
                poster = "https://i.pinimg.com/736x/40/29/19/402919bbe07931968ccc2d4627042e23.jpg",
                styleConfig = StyleConfigStream(
                    backgroundColor = cool_blue
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        FlipCard(
            "Example 3 - No UI when started",
            R.drawable.code_snippet_1,
        ) {

            BitmovinStream(
                streamId = TestStreamsIds.SINTEL,
                bitmovinStreamEventListener = object : BitmovinStreamEventListener {
                    var playerView by remember { mutableStateOf<PlayerView?>(null) }
                    override fun onPlayerReady(player: Player) {
                        player.on(PlayerEvent.Play::class.java) {
                            playerView?.isUiVisible = false
                        }
                    }

                    override fun onPlayerViewReady(playerView: PlayerView) {
                        this.playerView = playerView
                    }
                },
                enableAds = false
            )
        }
    }
}

@Composable
fun PropertyCard(prop: Pair<String, String>) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = prop.first,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
            )
            Text(
                text = prop.second,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
val cool_blue = Color(210,230,255)
@Composable
fun FlipCard(
    title: String,
    code: Int,
    aspectRatio : Float = 16f / 9f,
    player: @Composable () -> Unit

) {
    var isFront by remember { mutableStateOf(true) }


    Box(
        modifier = Modifier.padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(10.dp))
                .background(cool_blue),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(modifier = Modifier
                .fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(16.dp, 6.dp)
                        .fillMaxWidth()
                    ,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
            Box(modifier = Modifier
                .aspectRatio(aspectRatio)
                .padding(8.dp, 0.dp)) {
                if (isFront) {
                    Image(
                        painter = painterResource(id = code),
                        contentDescription = "Source Code",
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                } else {
                    Box {
                        player()
                    }
                }
            }


            Button(onClick = { isFront = !isFront }, modifier = Modifier
                .padding(8.dp, 0.dp, 8.dp, 8.dp)
                .fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                // make it colored cool blue
                colors = ButtonDefaults.buttonColors(Color(100,200,255))
            ) {
                Text(text = if (isFront) "Execute" else "Back to code preview", color = Color.Black)
            }
        }
    }
}

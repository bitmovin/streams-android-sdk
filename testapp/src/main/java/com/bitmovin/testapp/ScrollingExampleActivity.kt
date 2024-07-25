package com.bitmovin.testapp

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.bitmovin.streams.config.FullscreenConfig
import com.bitmovin.streams.config.StreamConfig
import com.bitmovin.streams.config.StreamError
import com.bitmovin.streams.config.StreamListener
import com.bitmovin.streams.config.StyleConfigStream
import com.bitmovin.testapp.ui.theme.StreamsandroidsdkTheme
import com.bitmovin.testapp.utils.TestStreamsIds

class ScrollingExampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StreamsandroidsdkTheme {
                BitmovinShowcase()
            }
        }
    }
}

@Composable
fun BitmovinShowcase() {
    val scrollState = rememberScrollState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column(
        modifier =
        Modifier
            .verticalScroll(scrollState)
            .fillMaxWidth()
            .padding(if (isLandscape) 144.dp else 0.dp, 0.dp)
            .safeContentPadding(),
    ) {
        Text(
            text = "Discovering the Bitmovin Stream Player Component",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier =
                Modifier
                    .padding(16.dp),
        )

        Text(
            text = "Using it",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp, 0.dp),
        )

        Text(
            text =
                """
                This demonstration showcases the capabilities of the Bitmovin Stream Player component in an Android app.
                To create a Stream Player, you should call the BitmovinStream composable function and pass the streamId as parameters from a ComposeView. That's all folks.
                """.trimIndent(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )

        FlipCard(
            "Basic Stream Player",
            R.drawable.code_snippet_1,
        ) {
            // Simplest way to use the Bitmovin Stream Player component
            BitmovinStream(
                streamId = TestStreamsIds.TEAR_OF_STEEL,
            )
        }

        Text(
            text =
                """
                Just like that, the component will setup itself with the stream's dashboard configuration properties.
                Warning : If your stream requires a token, you should pass it as a parameter to the BitmovinStream function.
                """.trimIndent(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )

        Text(
            text = "Android Stream Player properties and capabilities",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp, 0.dp),
        )
        Text(
            text =
                """
                There are many properties and capabilities that you can use to customize the player dynamically.
                
                Here is a quick list of properties that you can use.
                
                """.trimIndent(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )

        Text(
            text = "Mandatory properties",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
        )

        val streamIdProp =
            Property(
                "streamId",
                "The id of the stream to be played. All of the dashboard configurations of the stream will be applied to the player.",
            )
        PropertyCard(streamIdProp)

        Text(
            text = "Optional properties",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
        )

        repeat(optProperties.size) {
            PropertyCard(optProperties[it])
            if (it < optProperties.size - 1 && it > 0) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Text(
            text =
                """
                Here are some examples of how you can take advantage of the different properties to sweet your own need.
                Please note that plenty of options are feasible through the dashboard also feasible through the dashboard options.
                """.trimIndent(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )

        FlipCard(
            "Example 1 - Gif-like player",
            R.drawable.carbon_1,
        ) {
            val reusableGifVidConfig =
                StreamConfig(
                    streamId = TestStreamsIds.SQUARE_VIDEO,
                    autoPlay = true,
                    muted = true,
                    loop = true,
                    fullscreenConfig =
                        FullscreenConfig(
                            enable = false,
                        ),
                    onStreamReady = { _, playerView ->
                        playerView.isUiVisible = false
                    },
                )
            BitmovinStream(config = reusableGifVidConfig)
        }

        Spacer(modifier = Modifier.height(16.dp))

        FlipCard(
            "Example 2 - Styled Vertical Video",
            R.drawable.carbon_2,
            aspectRatio = 1.3f,
        ) {
            // Custom style configuration
            val myStyleConfig =
                StyleConfigStream(
                    playbackMarkerBorderColor = Color(0xFF01295F),
                    playbackMarkerBgColor = Color(0xAF01295F),
                    playbackTrackBufferedColor = Color(0x8FC5FFFD),
                    textColor = Color(0xFF88D9E6),
                    playbackTrackBgColor = Color(0xAF8B8BAE),
                    playbackTrackPlayedColor = Color(0xFF526760),
                    backgroundColor = cool_blue,
                )

            // Component
            BitmovinStream(
                streamId = TestStreamsIds.VERTICAL_VIDEO,
                poster = "https://i.pinimg.com/736x/40/29/19/402919bbe07931968ccc2d4627042e23.jpg",
                styleConfig = myStyleConfig,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        FlipCard(
            "Example 3 - Fullscreen on Play",
            R.drawable.carbon_3,
            aspectRatio = 1.5f,
        ) {
            BitmovinStream(
                streamId = TestStreamsIds.SINTEL,
                streamListener =
                    object : StreamListener {
                        override fun onStreamReady(
                            player: Player,
                            playerView: PlayerView,
                        ) {
                            player.on(PlayerEvent.Play::class.java) {
                                playerView.enterFullscreen()
                            }
                            player.on(PlayerEvent.Paused::class.java) {
                                playerView.exitFullscreen()
                            }
                        }

                        override fun onStreamError(error: StreamError) {
                            // Handle the error... error.toString() to get the error message
                        }
                    },
                enableAds = false,
            )
        }

        Spacer(Modifier.height(16.dp))

        FlipCard(
            "Example 4 - Some UI elements hidden",
            R.drawable.carbon_4,
            aspectRatio = 1.4f,
        ) {
            BitmovinStream(
                streamId = TestStreamsIds.SINTEL,
                styleConfig =
                    StyleConfigStream(
                        customCss =
                            // Please refer to https://developer.bitmovin.com/playback/docs/player-ui-css-class-reference
                            """
                            .bmpui-ui-volumetogglebutton {
                                display: none;
                            }
                            .bmpui-ui-settingstogglebutton {
                                display: none;
                            }
                            """.trimIndent(),
                    ),
            )
        }
    }
}

@Composable
fun PropertyCard(prop: Property) {
    Card(
        modifier =
        Modifier
            .padding(8.dp)
            .fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row {
                Text(
                    text = prop.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier,
                )
                prop.defaultValue?.let {
                    Row(
                        horizontalArrangement = Arrangement.Absolute.Right,
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                    ) {
                        Text(text = "Default :", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Text(
                text = prop.description,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

val cool_blue = Color(210, 230, 255)

@Composable
fun FlipCard(
    title: String,
    code: Int,
    aspectRatio: Float = 16f / 9f,
    player: @Composable () -> Unit,
) {
    var isFront by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier.padding(0.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(10.dp))
                .background(cool_blue),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier =
                    Modifier
                        .padding(16.dp, 6.dp)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
            Box(
                modifier =
                Modifier
                    .aspectRatio(aspectRatio)
                    .padding(8.dp, 0.dp),
            ) {
                val corners = 8.dp
                if (isFront) {
                    Image(
                        painter = painterResource(id = code),
                        contentDescription = "Source Code",
                        modifier =
                        Modifier
                            .clip(shape = RoundedCornerShape(corners))
                            .fillMaxWidth(),
                        contentScale = ContentScale.FillWidth,
                        alignment = Alignment.TopEnd,
                    )
                } else {
                    Box(Modifier.clip(shape = RoundedCornerShape(corners))) {
                        player()
                    }
                }
            }

            Button(
                onClick = { isFront = !isFront },
                modifier =
                    Modifier
                        .padding(8.dp, 0.dp, 8.dp, 8.dp)
                        .fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(Color(100, 200, 255)),
            ) {
                Text(text = if (isFront) "Execute" else "Back to code preview", color = Color.Black)
            }
        }
    }
}

data class Property(
    val name: String,
    val description: String,
    val defaultValue: String? = null,
)

val optProperties: List<Property> =
    listOf(
        Property(
            name = "modifier",
            description =
                """
                The modifier to be applied to the player. 
                Warning : It does not support all the modifiers available in Compose.
                """.trimIndent(),
            defaultValue = "Modifier",
        ),
        Property(
            name = "jwToken",
            description =
                """
                The token to be used for authentication if the stream is protected.
                If the token is not provided for a stream which needs it, A 401 error will appear on the player. 
                """.trimIndent(),
            defaultValue = "null",
        ),
        Property(
            name = "autoPlay",
            description =
                """
                Whether the player should start playing automatically.
                """.trimIndent(),
            defaultValue = "false",
        ),
        Property(
            name = "loop",
            description =
                """
                Whether the player should loop the stream.
                """.trimIndent(),
            defaultValue = "false",
        ),
        Property(
            name = "muted",
            description =
                """
                Whether the player should be muted.
                """.trimIndent(),
            defaultValue = "false",
        ),
        Property(
            name = "poster",
            description =
                """
                The poster image to be displayed before the player starts. 
                This property has priority over the poster image from the dashboard.
                If the poster is not provided neither in the dashboard nor in the player, the poster image will be an image from the video.
                """.trimIndent(),
            defaultValue = "null",
        ),
        Property(
            name = "start",
            description =
                """
                The time in seconds at which the player marker should start.
                """.trimIndent(),
            defaultValue = "0.0",
        ),
        Property(
            name = "subtitles",
            description =
                """
                The list of subtitle tracks available for the stream.
                """.trimIndent(),
            defaultValue = "Empty list",
        ),
        Property(
            name = "streamEventListener",
            description =
                """
                The listener for the player events.
                
                This is the gateway to modify the internal behaviour behavior.
                - onStreamReady : Called when the stream is ready to be played and pass the Player and PlayerView instances.
                - onStreamError : Called when an error occurs and pass the error code and message.
                    This callback only works for the stream setup errors. For the player errors, you should use the PlayerEvent.Error callback.
                
                Warning : The Stream Component may not work properly for some of the modification of the Player / PlayerView. Please consider using the Bitmovin Player SDK directly if you need a deep control over the player behavior.
                
                Problematic modifications include (but are not limited to) :
                - Changing the Fullscreen handler
                - Changing the Picture in Picture handler
                """.trimIndent(),
            defaultValue = "null",
        ),
        Property(
            name = "enableAds",
            description =
                """
                Whether ads should be enabled.
                Ads are retrieved from the stream's dashboard configuration.
                """.trimIndent(),
            defaultValue = "true",
        ),
        Property(
            name = "fullscreenConfig",
            description =
                """
                The configuration for the fullscreen mode.
                """.trimIndent(),
            defaultValue = "None",
        ),
        Property(
            name = "styleConfig",
            description =
                """
                The style configuration for the player.
                This property has priority over the style configuration from the dashboard.
                """.trimIndent(),
            defaultValue = "None",
        ),
    )

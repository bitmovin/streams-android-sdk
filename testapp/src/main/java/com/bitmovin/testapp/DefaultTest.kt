package com.bitmovin.testapp

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitmovin.streams.BitmovinStream
import com.bitmovin.streams.TestStreamsIds
import com.bitmovin.streams.config.StreamConfig
import com.bitmovin.streams.config.FullscreenConfig
import com.bitmovin.streams.config.StreamThemes
import com.bitmovin.streams.config.StyleConfigStream

class DefaultTest : ComponentActivity() {
    lateinit var fetchingTests: List<Test>
    lateinit var formatTests: List<Test>
    lateinit var fullscreenTests: List<Test>
    lateinit var styleTests: List<Test>
    lateinit var propertyTests : List<Test>
    override fun onCreate(savedInstanceState: Bundle?) {
        initTests()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val scrollState = rememberScrollState()
            var selectedTest by remember { mutableStateOf(fullscreenTests.first()) }
            Column(Modifier.safeDrawingPadding()) {
                Text(text = selectedTest.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(4.dp))
                key(selectedTest){
                    BitmovinStream(config = selectedTest.config)
                }
                Text(text = selectedTest.expectedResult, fontSize = 16.sp, modifier = Modifier.padding(4.dp), maxLines = 2, minLines = 2)
                Column(Modifier.verticalScroll(scrollState)) {
                    TestRow("Error handling", fetchingTests) { test -> selectedTest = test }
                    TestRow("Aspect Ratio ", tests = formatTests) { test -> selectedTest = test }
                    TestRow("Fullscreen Behaviour", tests = fullscreenTests) { test -> selectedTest = test }
                    TestRow("Styling", tests = styleTests) { test -> selectedTest = test }
                    TestRow("Properties", tests = propertyTests) { test -> selectedTest = test }
                }
            }
        }
    }


    fun initTests() {
        fetchingTests = listOf(
            Test(
                title = "Stream",
                expectedResult = "The stream should be displayed with all of the dashboard settings",
                config = StreamConfig(streamId = TestStreamsIds.TEAR_OF_STEEL)
            ),
            Test(
                title = "404 (Stream not found)",
                expectedResult = "Stream not found",
                config = StreamConfig(streamId = TestStreamsIds.SQUARE_VIDEO + "d")
            ),
            Test(
                title = "401 (Unauthorized)",
                expectedResult = "Unauthorized",
                config = StreamConfig(streamId = TestStreamsIds.BIG_BUCK_BUNNY)
            ),
            Test(
                title = "403 (Forbidden)",
                expectedResult = "Forbidden",
                config = StreamConfig(streamId = TestStreamsIds.BIG_BUCK_BUNNY, jwToken = "nonvalid")
            )
        )

        formatTests = listOf(
            Test(
                title = "1:1",
                expectedResult = "1:1",
                config = StreamConfig(streamId = TestStreamsIds.SQUARE_VIDEO)
            ),
            Test(
                title = "16:9",
                expectedResult = "16:9",
                config = StreamConfig(streamId = TestStreamsIds.SINTEL)
            ),
            Test(
                title = "9:16",
                expectedResult = "9:16",
                config = StreamConfig(streamId = TestStreamsIds.VERTICAL_VIDEO)
            )
        )

        fullscreenTests = listOf(
            Test(
                title = "Default",
                expectedResult = "Default Behaviour",
                config = StreamConfig(streamId = TestStreamsIds.SINTEL)
            ),
            Test(
                title = "Disabled",
                expectedResult = "Fullscreen and Picture-in-Picture should not be available",
                config = StreamConfig(streamId = TestStreamsIds.SINTEL, fullscreenConfig = FullscreenConfig(enable = false))
            ),
            Test(
                title = "Auto Orientation Disabled",
                expectedResult = "The screen should not rotate by itself when the Video is in Fullscreen mode",
                config = StreamConfig(streamId = TestStreamsIds.SINTEL, fullscreenConfig = FullscreenConfig(autoRotate = false))
            )
        )
        styleTests = listOf(
            Test(
                title = "Red theme",
                expectedResult = "A red theme should be applied to the player",
                config = StreamConfig(streamId = TestStreamsIds.SINTEL, styleConfig = StreamThemes.RED_EXAMPLE_THEME)
            ),
            Test(
                title = "Default Theme",
                expectedResult = "The default theme should be applied to the player",
                config = StreamConfig(streamId = TestStreamsIds.SINTEL, styleConfig = StreamThemes.BITMOVIN_DEFAULT_THEME)
            ),
            Test(
                title = "Plain CSS",
                expectedResult = "The player should show without it's settings button and volume button",
                config = StreamConfig(streamId = TestStreamsIds.SINTEL, styleConfig = StyleConfigStream(customCss = """
                    .bmpui-ui-volumetogglebutton {
                        display: none;
                    }
                    .bmpui-ui-settingstogglebutton {
                        display: none;
                    }
                """.trimIndent()
                ))
            )
        )
        propertyTests = listOf(
            Test(
                "Auto Play",
                "The video should start playing automatically",
                StreamConfig(streamId = TestStreamsIds.SINTEL, autoPlay = true)
            ),
            Test(
                "Loop",
                "The video should loop indefinitely",
                StreamConfig(streamId = TestStreamsIds.SQUARE_VIDEO, loop = true)
            ),
            Test(
                "Muted",
                "The video should be muted by default",
                StreamConfig(streamId = TestStreamsIds.SINTEL, muted = true)
            ),
            Test(
                "Start Time",
                "The video should start at 10 seconds",
                StreamConfig(streamId = TestStreamsIds.SINTEL, start = 10.0)
            ),
        )
    }
}

@Composable
fun TestRow(testCategory: String, tests: List<Test>, onTestSelected: (Test) -> Unit) {
    Text(text = testCategory, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(4.dp))
    Row(Modifier.horizontalScroll(rememberScrollState())) {
        tests.forEach { test ->
            Button(
                onClick = { onTestSelected(test) },
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0, 128, 237),
                    contentColor = Color.Unspecified,
                    disabledContainerColor = Color.Unspecified,
                    disabledContentColor = Color.Unspecified,
                )
            ) {
                Text(text = test.title)
            }
        }
    }
}
class Test(
    val title: String,
    val expectedResult: String,
    val config: StreamConfig
)

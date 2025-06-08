package net.amazingdomain.octo_flashforge.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.amazingdomain.octo.gcode.ScreenMonitor
import net.amazingdomain.octo_flashforge.android.ui.configuration.ScreenConfiguration
import net.amazingdomain.octo_flashforge.android.ui.video.ScreenVideo
import timber.log.Timber


@Composable
fun ScreenMain(
    temperatureState: State<Int?>?,
    videoUrlState: State<String?>,
) {


    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(800.dp)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        var configurationReloadCount by remember { mutableStateOf(0) }

        ScreenMonitor(temperatureState?.value)

        videoUrlState.value
            ?.let {
                ScreenVideo(url = it)
            }
            ?: Text("No video URL found")

        ScreenConfiguration(

            // TODO we get this notification but we yet don't trigger the updates necessary to both video and monitor
            onConfigurationChanged = {
                configurationReloadCount++
                Timber.d("Configuration reloaded: $configurationReloadCount times")

            },
        )


    }
}

@Preview(device = "spec:width=300dp,height=300dp")
@Composable
private fun PreviewScreenMain() {
    ScreenMain(
        temperatureState = remember { mutableStateOf(210) },
        videoUrlState = remember { mutableStateOf("http://example.com/video.mp4") },
    )
}

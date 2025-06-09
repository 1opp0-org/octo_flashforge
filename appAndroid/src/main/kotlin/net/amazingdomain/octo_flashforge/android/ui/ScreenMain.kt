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
import net.amazingdomain.octo.crossplatform.ui.ScreenMonitor
import net.amazingdomain.octo_flashforge.android.ui.configuration.ConfigurationRepository.ConfigurationInfo
import net.amazingdomain.octo_flashforge.android.ui.configuration.ScreenConfiguration
import net.amazingdomain.octo_flashforge.android.ui.video.ScreenVideo
import timber.log.Timber


@Composable
fun ScreenMain(
    configurationInfoState: State<ConfigurationInfo?>,
    temperatureState: State<Int?>?,
    videoUrlState: State<String?>,
    onDefaultConfigurationChanged: (ConfigurationInfo) -> Unit,
    onConfigurationSaved: (ConfigurationInfo) -> Unit,
    loadAllConfigurations: () -> List<ConfigurationInfo>,
) {


    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(800.dp)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        ScreenMonitor(temperatureState?.value)

        videoUrlState.value
            ?.let {
                ScreenVideo(url = it)
            }
            ?: Text("No video URL found")

        ScreenConfiguration(
            configurationInfo = configurationInfoState.value,
            onConfigurationSaved = onConfigurationSaved,
            onDefaultConfigurationChanged = onDefaultConfigurationChanged,
            loadAllConfigurations = loadAllConfigurations


        )


    }
}

@Preview(device = "spec:width=300dp,height=300dp", showBackground = true)
@Composable
private fun PreviewScreenMain() {
    ScreenMain(
        configurationInfoState = remember { mutableStateOf(null) },
        temperatureState = remember { mutableStateOf(210) },
        videoUrlState = remember { mutableStateOf("http://example.com/video.mp4") },
        onConfigurationSaved = {},
        onDefaultConfigurationChanged = {},
        loadAllConfigurations = { listOf() },
    )
}

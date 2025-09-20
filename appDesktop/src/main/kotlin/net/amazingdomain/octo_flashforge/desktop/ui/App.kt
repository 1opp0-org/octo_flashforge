package net.amazingdomain.octo_flashforge.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.amazingdomain.octo.crossplatform.ui.ScreenMonitor
import net.amazingdomain.octo.gcode.MonitorUseCase
import net.amazingdomain.octo.ui.ImageLogo
import net.amazingdomain.octo_flashforge.desktop.ui.video.ScreenVideo

@Composable
@Preview
fun App(monitorUseCase: MonitorUseCase) {

    val text = remember { mutableStateOf("Hello, Desktop World!") }

    val gcodeResponse = monitorUseCase
        .sharedFlow
        .collectAsState(null)

    MaterialTheme {

        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(onClick = {
                    text.value = "Hello, Desktop!"
                }) {
                    Text(text.value)
                }

                Box(
                    modifier = Modifier.Companion.size(60.dp)
                ) {
                    ImageLogo()
                }
            }

            Column {
                ScreenMonitor(gcodeResponse.value)
                ScreenVideo()
            }

        }
    }
}
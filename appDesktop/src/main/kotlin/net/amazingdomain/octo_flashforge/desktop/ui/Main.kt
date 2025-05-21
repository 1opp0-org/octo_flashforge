package net.amazingdomain.octo_flashforge.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.amazingdomain.octo.gcode.MonitorRepository
import net.amazingdomain.octo.gcode.MonitorUseCase
import net.amazingdomain.octo.gcode.ScreenMonitor
import net.amazingdomain.octo_flashforge.desktop.ui.video.ScreenVideo

private val repository = MonitorRepository(host = "127.0.0.1", port = 8899)
private val useCaseMonitorTemperature = MonitorUseCase(repository)

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, Desktop World!") }


    MaterialTheme {

        var temperature by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(Unit ) {
            temperature = useCaseMonitorTemperature
                .getExtruderTemperature()
        }

        Column {

            Button(onClick = {
                text = "Hello, Desktop!"
            }) {
                Text(text)
            }

            Column {
                ScreenMonitor(temperature)
                Button(onClick = {
                    CoroutineScope(Dispatchers.IO)
                        .launch {
                            temperature = useCaseMonitorTemperature
                                .getExtruderTemperature()
                        }
                }) {
                    Text("Refresh Temperature")
                }
                ScreenVideo()
            }

        }
    }
}

fun main() = application {
    val windowState = rememberWindowState()

    windowState.size = DpSize(800.dp, 900.dp)
    windowState.position = WindowPosition(400.dp, 400.dp)

    Window(
        title = "Octo Flashforge",
        state = windowState,
        onCloseRequest = ::exitApplication,
    ) {
        App()
    }
}


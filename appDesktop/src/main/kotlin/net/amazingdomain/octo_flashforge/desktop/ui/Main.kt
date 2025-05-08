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
import net.amazingdomain.octo.gcode.ScreenMonitor
import net.amazingdomain.octo_flashforge.desktop.ui.video.ScreenVideo


@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, Desktop World!") }

    MaterialTheme {
        Column {

            Button(onClick = {
                text = "Hello, Desktop!"
            }) {
                Text(text)
            }

            Column {
                ScreenMonitor()
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


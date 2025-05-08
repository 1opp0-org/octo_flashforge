package net.amazingdomain.octo_flashforge.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


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
                ScreenVideo()
            }

        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}



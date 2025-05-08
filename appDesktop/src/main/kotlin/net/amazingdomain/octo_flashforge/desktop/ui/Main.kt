package net.amazingdomain.octo_flashforge.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import net.amazingdomain.octo.attempt2.HelloWorld3


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

            HelloWorld3()
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}



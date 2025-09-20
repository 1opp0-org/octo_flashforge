package net.amazingdomain.octo_flashforge.desktop.ui


import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import mu.KotlinLogging
import net.amazingdomain.octo.crossplatform.ui.ScreenMonitor
import net.amazingdomain.octo.gcode.MonitorUseCase
import net.amazingdomain.octo.networking.ClientSocket
import net.amazingdomain.octo.ui.ImageLogo
import net.amazingdomain.octo_flashforge.crossplatform.ui.resources.Res
import net.amazingdomain.octo_flashforge.crossplatform.ui.resources.logo
import net.amazingdomain.octo_flashforge.desktop.ui.video.ScreenVideo
import org.jetbrains.compose.resources.painterResource


private val statusUpdateIntervalMs = 2000L
private val repository =
    ClientSocket(
        host = "127.0.0.1", port = 8899,
        disconnectTimeoutMs = statusUpdateIntervalMs * 2,
    )
private val useCaseMonitorTemperature = MonitorUseCase(repository)

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, Desktop World!") }

    val temperatureState = useCaseMonitorTemperature
        .getExtruderTemperatureFlow(statusUpdateIntervalMs)
        .collectAsState(null)


    MaterialTheme {

        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(onClick = {
                    text = "Hello, Desktop!"
                }) {
                    Text(text)
                }

                Box(
                    modifier = Modifier.size(60.dp)
                ) {
                    ImageLogo()
                }
            }

            Column {
                ScreenMonitor(temperatureState.value)

                ScreenVideo()
            }

        }
    }
}

fun main() = application {

    val logger = KotlinLogging.logger {}

    logger.info("Application start")

    val windowState = rememberWindowState()

    windowState.size = DpSize(800.dp, 900.dp)
    windowState.position = WindowPosition(400.dp, 400.dp)

    Window(
        title = "Octo Flashforge",
        state = windowState,
        onCloseRequest = ::exitApplication,
        icon = painterResource(Res.drawable.logo),
    ) {
        App()
    }
}

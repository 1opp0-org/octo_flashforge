package net.amazingdomain.octo_flashforge.desktop.ui


import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import mu.KotlinLogging
import net.amazingdomain.octo.gcode.MonitorUseCase
import net.amazingdomain.octo.networking.ClientSocket
import net.amazingdomain.octo_flashforge.crossplatform.ui.resources.Res
import net.amazingdomain.octo_flashforge.crossplatform.ui.resources.logo
import org.jetbrains.compose.resources.painterResource


fun main(args: Array<String>) = application {

    val logger = KotlinLogging.logger {}

    var host = "127.0.0.1"
    var port = 8899
    val statusUpdateIntervalMs = 2000L

    for (i in args.indices) {
        if (args[i] == "--host" && i + 1 < args.size) {
            host = args[i + 1]
        }
        if (args[i] == "--port" && i + 1 < args.size) {
            port = args[i + 1].toIntOrNull() ?: 8899
        }
    }

    logger.info("Application start on '$host:$port'")

    val repository =
        ClientSocket(
            host = host, port = port,
            disconnectTimeoutMs = statusUpdateIntervalMs * 2,
        )
    val useCaseMonitorTemperature = MonitorUseCase(repository)

    val windowState = rememberWindowState()

    windowState.size = DpSize(800.dp, 900.dp)
    windowState.position = WindowPosition(100.dp, 400.dp)

    Window(
        title = "Octo Flashforge",
        state = windowState,
        onCloseRequest = ::exitApplication,
        icon = painterResource(Res.drawable.logo),
    ) {
        App(useCaseMonitorTemperature, statusUpdateIntervalMs)
    }
}

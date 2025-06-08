package net.amazingdomain.octo_flashforge.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.amazingdomain.octo.networking.ClientSocket
import net.amazingdomain.octo.gcode.MonitorUseCase
import net.amazingdomain.octo.gcode.ScreenMonitor
import net.amazingdomain.octo_flashforge.android.ui.configuration.ConfigurationRepository
import net.amazingdomain.octo_flashforge.android.ui.configuration.ScreenConfiguration
import net.amazingdomain.octo_flashforge.android.ui.video.ScreenVideo
import net.amazingdomain.octo_flashforge.theme.OctoTheme
import timber.log.Timber

// TODO: extract user facing strings into proper file/structure to be shared with Desktop app
/**
 * Activity launched at app start
 */
class MainActivity : ComponentActivity() {

    companion object {
        const val MONITOR_INTERVAL_MS = 1000L
        const val DISCONNECT_TIMEOUT_MS = MONITOR_INTERVAL_MS * 2
    }

    private lateinit var configurationRepository: ConfigurationRepository

    private var useCaseMonitorTemperature: MonitorUseCase? = null
    private var monitorRepository: ClientSocket? = null

    // TODO find a way to reconnect on resume
    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO)
            .launch {
                monitorRepository?.ensureConnection()
            }
    }

    override fun onPause() {
        super.onPause()
        CoroutineScope(Dispatchers.IO)
            .launch {
                monitorRepository?.disconnect()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO this should be done by hilt
        configurationRepository = ConfigurationRepository(applicationContext = applicationContext)

        monitorRepository = with(configurationRepository) {
            getGcodeIpAddress() to getGcodeIpPort()
        }
            .let {
                val host = it.first
                val port = it.second
                if (host != null && port != null) {
                    ClientSocket(
                        host = host,
                        port = port,
                        disconnectTimeoutMs = DISCONNECT_TIMEOUT_MS
                    )
                } else {
                    Timber.e("Gcode IP address or port is not set in configuration")
                    null
                }
            }

        setContent {

            val temperatureState = monitorRepository
                ?.let { MonitorUseCase(monitorRepository = it) }
                ?.getExtruderTemperatureFlow(MONITOR_INTERVAL_MS)
                ?.collectAsState(null)

            val videoUrlState = remember {
                mutableStateOf(configurationRepository.getVideoUrl())
            }

            OctoTheme {
                Scaffold(
                    topBar = TopBar, modifier = Modifier.fillMaxWidth()
                ) { innerPadding ->

                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {

                        ScreenMain(
                            temperatureState = temperatureState,
                            videoUrlState = videoUrlState,
                        )
                    }
                }
            }


        }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    private val TopBar: @Composable () -> Unit = {

        TopAppBar(title = { Text("Octo Flash Forge") })
    }

    @Preview
    @Composable
    private fun TopBarPreview() {
        TopBar()
    }
}

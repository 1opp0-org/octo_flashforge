package net.amazingdomain.octo_flashforge.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.amazingdomain.octo.crossplatform.ui.ImageLogo
import net.amazingdomain.octo.gcode.MonitorUseCase
import net.amazingdomain.octo.networking.ClientSocket
import net.amazingdomain.octo_flashforge.android.ui.configuration.ConfigurationRepository
import net.amazingdomain.octo_flashforge.android.ui.configuration.buildVideoUrl
import net.amazingdomain.octo_flashforge.android.ui.configuration.getGcodeIpAddress
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

    private var configurationRepository: ConfigurationRepository? = null

    private var monitorRepository: ClientSocket? = null

    // TODO find a way to reconnect on resume
    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.IO)
            .launch {
                setupActivity()
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

        setupActivity()

        setContent {
            Content()
        }

    }

    private fun setupActivity() {

        Timber.d("Setting up MainActivity")
        // TODO this should be done by hilt
        configurationRepository = ConfigurationRepository(applicationContext = applicationContext)

        val configurationInfo = configurationRepository?.loadActiveConfiguration()

        monitorRepository = configurationInfo
            ?.let {
                ClientSocket(
                    host = it.getGcodeIpAddress(),
                    port = it.gcodePort,
                    disconnectTimeoutMs = DISCONNECT_TIMEOUT_MS
                )
            }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    private val TopBar: @Composable () -> Unit = {

        TopAppBar(title = {
            val topAppBarHeight = TopAppBarDefaults.MediumAppBarCollapsedHeight

            Row {
                Box(
                    modifier = Modifier
                        .height(height = topAppBarHeight / 2)
                        .padding(end = 16.dp)
                ) {
                    ImageLogo()
                }
                Text("Octo Flash Forge")
            }
        })
    }

    @Composable
    fun Content() {
        val configurationInfoState = remember {
            mutableStateOf<ConfigurationRepository.ConfigurationInfo?>(null)
        }

        // TODO run this again when configuration changes
        val temperatureState = monitorRepository
            ?.let { MonitorUseCase(monitorRepository = it) }
            ?.getExtruderTemperatureFlow(MONITOR_INTERVAL_MS)
            ?.collectAsState(null)

        val videoUrlState = remember {
            mutableStateOf<String?>(null)
        }

        val onConfigurationSaved: (ConfigurationRepository.ConfigurationInfo) -> Unit =
            { info ->
                configurationRepository?.saveConfiguration(
                    label = info.label,
                    ipAddress = info.ipAddress
                )
            }

        val onDefaultConfigurationChanged: (ConfigurationRepository.ConfigurationInfo?) -> Unit =
            { newConfiguration ->

                newConfiguration?.label
                    ?.let { configurationRepository?.saveActiveLabel(it) }

                configurationInfoState.value = newConfiguration

                videoUrlState.value = newConfiguration?.buildVideoUrl()

                setupActivity()

            }

        onDefaultConfigurationChanged(configurationRepository?.loadActiveConfiguration())



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
                        configurationInfoState = configurationInfoState,
                        temperatureState = temperatureState,
                        videoUrlState = videoUrlState,
                        onConfigurationSaved = onConfigurationSaved,
                        onDefaultConfigurationChanged = onDefaultConfigurationChanged,
                        loadAllConfigurations = {
                            configurationRepository?.loadAllConfigurations() ?: emptyList()
                        }

                    )
                }
            }
        }
    }

    // region Preview

    @Preview
    @Composable
    private fun TopBarPreview() {
        TopBar()
    }

    @Preview
    @Composable
    fun ContentPreview() {
        OctoTheme {
            Content()
        }
    }

    // endregion Preview
}

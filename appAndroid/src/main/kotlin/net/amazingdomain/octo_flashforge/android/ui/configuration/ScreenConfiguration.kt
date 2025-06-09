package net.amazingdomain.octo_flashforge.android.ui.configuration

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.amazingdomain.octo_flashforge.android.ui.configuration.ConfigurationRepository.ConfigurationInfo
import net.amazingdomain.octo_flashforge.android.ui.configuration.ConfigurationRepository.ConfigurationInfo.Companion.DEFAULT_GCODE_PORT
import net.amazingdomain.octo_flashforge.android.ui.configuration.ConfigurationRepository.ConfigurationInfo.Companion.DEFAULT_VIDEO_PORT
import net.amazingdomain.octo_flashforge.theme.OctoTheme
import timber.log.Timber

// TODO extract strings into proper structure to be shared with Desktop app
// TODO refactor this into shared function to be used together with Desktop app
// TODO Introduce DI
// TODO add label to display webview state, since it always freezes when screen is powered off and on again on real device (not emulator?)
/**
 * Provides a text field to type in the IP address of the printer, a button to save it to shared preferences,
 * and automatically loads the saved IP address when the screen is displayed.
 */
@Composable
fun ScreenConfiguration(
    configurationInfo: ConfigurationInfo?,
    loadAllConfigurations: () -> List<ConfigurationInfo>,
    onDefaultConfigurationChanged: (ConfigurationInfo) -> Unit,
    onConfigurationSaved: (ConfigurationInfo) -> Unit,
) {
    Timber.d("Recompose content 4 $configurationInfo" )

    var label by remember { mutableStateOf(TextFieldValue(configurationInfo?.label ?: "NONE")) }

    var ipAddress by remember {
        mutableStateOf(
            TextFieldValue(
                configurationInfo?.ipAddress ?: "NONE"
            )
        )
    }

    var showDialog by remember { mutableStateOf(false) }


    LaunchedEffect(configurationInfo) {
        label = TextFieldValue(configurationInfo?.label ?: "NONE")
        ipAddress = TextFieldValue(configurationInfo?.ipAddress ?: "NONE")
    }


    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = label,
            onValueChange = {
                Timber.i("Label changed to: $it")
                label = it
            },
            label = { Text("Printer Label") },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = ipAddress,
            onValueChange = { ipAddress = it },
            label = { Text("Printer IP Address") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {

                val newConfigurationInfo = ConfigurationInfo(
                    label = label.text,
                    ipAddress = ipAddress.text,
                    gcodePort = -1,
                    videoPort = -1,
                )
                onConfigurationSaved(newConfigurationInfo)
            },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("Save")
        }

        Button(onClick = {
            showDialog = true
        }) {
            Text("Show All Configurations")
        }
    }

    if (showDialog) {

        ShowStoredLabels(
            loadAllConfigurations(),
            onSelect = { newDefaultConfiguration ->
                onDefaultConfigurationChanged(newDefaultConfiguration)
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            })
    }
}

@Composable
fun ShowStoredLabels(
    savedConfigs: List<ConfigurationInfo>,
    onSelect: (newDefaultConfiguration: ConfigurationInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surface,
            shape = AbsoluteRoundedCornerShape(16.dp) as Shape
        ),
        onDismissRequest = onDismiss,
        title = { Text("Saved Configurations") },
        text = {
            Column {
                savedConfigs.forEach { (savedLabel, savedIp) ->
                    Text(
                        text = "$savedLabel: $savedIp",
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = AbsoluteRoundedCornerShape(8.dp),
                            )

                            .clickable {
                                savedConfigs
                                    .firstOrNull { it.label == savedLabel }
                                    ?.let { onSelect(it) }


                            }
                            .padding(all = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// region previews
@Preview(showBackground = true)
@Composable
fun ScreenConfigurationPreview() {
    OctoTheme {

        ScreenConfiguration(
            configurationInfo = null,
            onDefaultConfigurationChanged = { },
            onConfigurationSaved = {},
            loadAllConfigurations = { emptyList() })
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ShowStoredLabelsPreview() {
    OctoTheme {

        ShowStoredLabels(
            savedConfigs = listOf(
                ConfigurationInfo(
                    "Printer 1",
                    "ip address 2",
                    gcodePort = DEFAULT_GCODE_PORT,
                    videoPort = DEFAULT_VIDEO_PORT,
                ),
                ConfigurationInfo(
                    "Printer 2",
                    "ip address 2",
                    gcodePort = DEFAULT_GCODE_PORT,
                    videoPort = DEFAULT_VIDEO_PORT,
                )
            ),
            onSelect = { _ -> },
            onDismiss = {}
        )
    }
}
// endregion previews


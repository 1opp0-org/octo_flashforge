package net.amazingdomain.octo_flashforge.android.ui.configuration

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// TODO convert [Log] to [Timber]
// TODO extract strings into proper structure to be shared with Desktop app
// TODO refactor this into shared function to be used together with Desktop app
// TODO Introduce DI
// TODO add label to display webview state, since it always freezes when screen is powered off and on again on real device (not emulator?)
/**
 * Provides a text field to type in the IP address of the printer, a button to save it to shared preferences,
 * and automatically loads the saved IP address when the screen is displayed.
 */
@Preview
@Composable
fun ScreenConfiguration() {

    val configurationRepository = ConfigurationRepository()

    val context = LocalContext.current
    var ipAddress by remember { mutableStateOf(TextFieldValue("")) }
    var label by remember { mutableStateOf(TextFieldValue("")) }
    var showDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit ) {
        // Load the saved IP address and label when the screen is displayed
        val (savedLabel, savedIp) = configurationRepository.loadActiveConfiguration(context)
            ?: Pair("NONE", "NONE")
        label = TextFieldValue(savedLabel)
        ipAddress = TextFieldValue(savedIp)
        Log.i("ScreenConfiguration", "LaunchedEffects Loaded saved IP: $savedIp, label: $savedLabel")
    }

    Log.i("ScreenConfiguration", "Recomposing the whole thing")

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = label,
            onValueChange = {
                Log.i("ScreenConfiguration", "Label changed to: $it")
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
            onClick = { configurationRepository.saveConfiguration(context, label.text, ipAddress.text) },
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
            configurationRepository.loadAllConfigurations(context),
            onSelect = { savedLabel ->
                configurationRepository.saveActiveLabel(context, savedLabel)

                val (_, _) = configurationRepository.loadActiveConfiguration(context)
                    ?: Pair("NONE", "NONE")
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            })
    }
}

@Preview
@Composable
fun ShowStoredLabels(
    savedConfigs: List<Pair<String, String>>,
    onSelect: (savedLabel: String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Saved Configurations") },
        text = {
            Column {
                savedConfigs.forEach { (savedLabel, savedIp) ->
                    Text(
                        text = "$savedLabel: $savedIp",
                        modifier = Modifier
                            .clickable {
                                onSelect(savedLabel)

                            }
                            .padding(vertical = 8.dp)
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

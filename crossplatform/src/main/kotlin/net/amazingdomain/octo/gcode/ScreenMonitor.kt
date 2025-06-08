package net.amazingdomain.octo.gcode

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun ScreenMonitor(temperature: Int?) {

    Box(modifier = Modifier.padding(16.dp)) {

        Column {

            Text("Hello monitor from crossplatform")
            Text("Temperature = " + (temperature ?: "unknown"))
        }
    }

}
package net.amazingdomain.octo_flashforge.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {


            Scaffold(
                topBar = TopBar,
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->

                Box(modifier = Modifier.padding(innerPadding)) {
                    Content()
                }
            }

        }

    }

    private val TopBar: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Text(text = "Octo Flash Forge")
        }
    }

    @Preview
    @Composable
    private fun TopBarPreview() {
        TopBar()
    }

    @Preview
    @Composable
    private fun Content() {
        val count = remember { mutableStateOf(0) }
        val copy = remember { mutableStateOf("Hello Android") }

        Box(modifier = Modifier.Companion.fillMaxSize()) {

            Button(onClick = {

                count.value++
                copy.value = "Hello Android! count= ${count.value}"

            }) {
                Text(copy.value)
            }
        }
    }
}
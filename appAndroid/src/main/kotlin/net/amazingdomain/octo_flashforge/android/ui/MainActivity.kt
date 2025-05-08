package net.amazingdomain.octo_flashforge.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.amazingdomain.octo.attempt2.HelloWorld3
import net.amazingdomain.octo_flashforge.android.ui.configuration.ConfigurationRepository
import net.amazingdomain.octo_flashforge.android.ui.configuration.ScreenConfiguration
import net.amazingdomain.octo_flashforge.android.ui.video.ScreenVideo
import net.amazingdomain.octo_flashforge.theme.OctoTheme

// TODO: extract user facing strings into proper file/structure to be shared with Desktop app
/**
 * Activity launched at app start
 */
class MainActivity : ComponentActivity() {

    lateinit var configurationRepository: ConfigurationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configurationRepository = ConfigurationRepository()

        setContent {

            OctoTheme {
                Scaffold(
                    topBar = TopBar, modifier = Modifier.fillMaxWidth()
                ) { innerPadding ->

                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        Content()
                    }
                }
            }


        }

    }

    @Preview(device = "spec:width=300dp,height=300dp")
    @Composable
    private fun Content() {

        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            configurationRepository.getVideoUrl(this@MainActivity)
                ?.let {
                    ScreenVideo(url = it)
                }
                ?: Text("No video URL found")

            HelloWorld3()

            ScreenConfiguration()

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

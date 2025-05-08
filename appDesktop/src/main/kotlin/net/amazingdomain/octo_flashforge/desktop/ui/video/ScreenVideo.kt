package net.amazingdomain.octo_flashforge.desktop.ui.video

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent

// TODO error handling if VLC is not installed/found
// TODO move user facing strings to shared file
/**
 * Following VLCJ tutorial from https://capricasoftware.co.uk/tutorials/vlcj/4/first-steps
 */
@Preview
@Composable
fun ScreenVideo() {

    val mediaPlayerComponent = remember { EmbeddedMediaPlayerComponent() }
    val mediaPlayer = remember { mediaPlayerComponent.mediaPlayer() }

    val factory = remember { { mediaPlayerComponent } }
    val isPlaying = remember { mutableStateOf(false) }


    val url = "http://127.0.0.1:9090?action=stream"

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    Column {
        Button(onClick = {


            if (isPlaying.value) {
                // if instead of stop we make it pause, it will keep streaming
                // from the network and will stutter badly when unpaused
                mediaPlayer.controls().stop()

            } else {
                mediaPlayer.controls().play()
            }

            isPlaying.value = !isPlaying.value
        }) {
            Text(
                text =
                    if (isPlaying.value) "Stop" else "Play"
            )
        }

        SwingPanel(
            factory = factory,
            update = {
                mediaPlayer.media().play(url)
                isPlaying.value = true
            },
            background = Color.Transparent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Gray)
                    .padding(16.dp)
        )
    }


}


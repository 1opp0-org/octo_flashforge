package net.amazingdomain.octo_flashforge.android.ui.video

import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

// TODO extract user facing strings into proper structure to be shared with Desktop app
// TODO re-implmement from scratch in Desktop app
/**
 * This video provides 2 ways to display from a URL, one with Exoplayer through [Video] and another fallback
 * using [WebViewVideo]. It displays a button at the top to let the user choose.
 */
@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenVideo(url: String) {
    val isWebView = remember { mutableStateOf(true) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Choose video display method",
                    style = typography.labelMedium
                )
                Text(
                    text = if (isWebView.value) "WebView" else "ExoPlayer",

                    style = typography.bodySmall
                )
            }
            Switch(
                checked = isWebView.value,
                onCheckedChange = { isWebView.value = it }
            )
        }

        Row(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                "URL",
                style = typography.labelMedium,
                modifier = Modifier.padding(end = 16.dp)
            )

            Text(
                url,
                style = typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .requiredHeightIn(max = 500.dp)
                .background(Color.DarkGray)
                .padding(2.dp)
                .background(Color.Gray)
        ) {

            if (isWebView.value) {
                WebViewVideo(url)
            } else {
                Video(url)
            }

        }
    }

}

/**
 * Play video from a url
 */
@OptIn(UnstableApi::class)
@Preview
@Composable
private fun Video(url: String) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }

    DisposableEffect(
        AndroidView(
            modifier = Modifier.requiredHeightIn(max = 500.dp),
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
            })
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}

/**
 * Loads a video through a webview. Useful for the video formats that Exoplayer doesn't recognize,
 * such as Codec: Motion JPEG Video (MJPG) used by Flashforge Adventurer 3 and other very cheap web cams.
 *
 * The [AndroidView::onRelease] callback could have called [Webview.loadUrl("about:blank")],
 * but instead calls [Webview.stopLoading()] since it allows faster recovery time if the view goes through
 * rendering, hiding and rendering again
 *
 * To make sure that the webview stops network traffic try redirecting your original stream through socat:
 *
 *          socat -d -d TCP-LISTEN:9090,fork TCP:192.168.0.xxx:8080
 *
 * Where socat is being verbose with `-d -d`  (yes)
 * it's listening for a connection on localhost port `9090` (remember emulators and real phones have their own localhost, so
 *     use `ifconfig` to find out the address of your local laptop ;  then edit `network_security_config.xml` to allow http without SSL)
 * `fork` is used to keep socat running, so you can close and reopen the connection as many times as the test needs
 * and it's connected to a video stream at 192.168.0.xxx:8080
 */
// TODO: detect dropped connection and display some error message
@Composable
private fun WebViewVideo(url: String, modifier: Modifier = Modifier) {


    AndroidView(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    MATCH_PARENT,
                    MATCH_PARENT
                )

                settings.apply {
                    // Enable zooming
                    builtInZoomControls = true
                    displayZoomControls = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
            }
        }, update = {
            Log.i("WebViewVideo", "update")
            it.loadUrl(url)
        },
        onReset = {
            Log.i("WebViewVideo", "reset")
        },
        onRelease = {
            Log.i("WebViewVideo", "release")
            it.apply {

                stopLoading()
            }

        })

}


@ExperimentalMaterial3Api
@Composable
fun MySlider(content: @Composable (sliderValue: Float) -> Unit) {

    val sliderState: SliderState = remember { SliderState(value = 1.0f, valueRange = 0.2f..2f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Center slider and text
    ) {
        Slider(
            state = sliderState,
            modifier = Modifier
                .fillMaxWidth()
        )

        Text(
            text = String.format("Value: %.1f", sliderState.value)
        )

        content(sliderState.value)
    }
}
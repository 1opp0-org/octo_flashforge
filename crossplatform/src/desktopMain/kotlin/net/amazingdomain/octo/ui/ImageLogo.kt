package net.amazingdomain.octo.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image as androidImage
import androidx.compose.runtime.Composable
import net.amazingdomain.octo_flashforge.crossplatform.ui.resources.Res
import net.amazingdomain.octo_flashforge.crossplatform.ui.resources.logo
import org.jetbrains.compose.resources.painterResource

@Preview
@Composable
fun ImageLogo(){

    androidImage( painter = painterResource(Res.drawable.logo,) , contentDescription = "",)
}
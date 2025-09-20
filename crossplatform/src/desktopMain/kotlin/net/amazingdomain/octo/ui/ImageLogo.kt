package net.amazingdomain.octo.ui


import androidx.compose.runtime.Composable
import net.amazingdomain.octo_flashforge.crossplatform.ui.resources.Res
import net.amazingdomain.octo_flashforge.crossplatform.ui.resources.logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image as androidImage

@Preview
@Composable
fun ImageLogo(){

    androidImage( painter = painterResource(Res.drawable.logo,) , contentDescription = "",)
}


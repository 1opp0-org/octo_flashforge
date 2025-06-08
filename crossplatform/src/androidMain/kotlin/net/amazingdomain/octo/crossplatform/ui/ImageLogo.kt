package net.amazingdomain.octo.crossplatform.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import net.amazingdomain.octo_flashforge.android.crossplatform.R
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun ImageLogo() {

    Image(painterResource(R.drawable.logo),"")
}

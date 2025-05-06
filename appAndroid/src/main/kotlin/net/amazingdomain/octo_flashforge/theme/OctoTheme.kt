package net.amazingdomain.octo_flashforge.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun OctoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = when {
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val typography = Typography(
//        titleMedium = MaterialTheme.typography.titleMedium.copy(),
    )


    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )

}


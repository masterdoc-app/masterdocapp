package pro.fixaverse.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import pro.fixaverse.design.theme.fixaverseLightColorScheme

val LocalFixaverseFontFamilies = staticCompositionLocalOf<FixaverseFontFamilies> {
    error("FixaverseFontFamilies not provided — wrap content in FixaverseTheme")
}

@Composable
fun FixaverseTheme(content: @Composable () -> Unit) {
    val fonts = rememberFixaverseFontFamilies()
    CompositionLocalProvider(LocalFixaverseFontFamilies provides fonts) {
        MaterialTheme(
            colorScheme = fixaverseLightColorScheme(),
            typography = fixaverseTypography(fonts),
            shapes = fixaverseShapes(),
            content = content,
        )
    }
}

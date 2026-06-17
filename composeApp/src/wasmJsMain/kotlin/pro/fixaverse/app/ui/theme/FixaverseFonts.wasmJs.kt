package pro.fixaverse.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily

@Composable
actual fun rememberFixaverseFontFamilies(): FixaverseFontFamilies = remember {
    FixaverseFontFamilies(
        sans = FontFamily.SansSerif,
        serif = FontFamily.Serif,
        mono = FontFamily.Monospace,
    )
}

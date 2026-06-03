package pro.masterdoc.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LiteColorScheme = lightColorScheme(
    primary = MasterdocPalette.Ink,
    onPrimary = MasterdocPalette.Paper,
    primaryContainer = MasterdocPalette.Paper2,
    onPrimaryContainer = MasterdocPalette.Ink,
    secondary = MasterdocPalette.Flare,
    onSecondary = MasterdocPalette.Paper,
    secondaryContainer = MasterdocPalette.FlareSoft,
    onSecondaryContainer = MasterdocPalette.Flare,
    tertiary = MasterdocPalette.Marian,
    onTertiary = MasterdocPalette.Paper,
    tertiaryContainer = MasterdocPalette.ForestSoft,
    onTertiaryContainer = MasterdocPalette.Forest,
    background = MasterdocPalette.Paper,
    onBackground = MasterdocPalette.Ink,
    surface = MasterdocPalette.Paper,
    onSurface = MasterdocPalette.Ink,
    surfaceVariant = MasterdocPalette.Paper2,
    onSurfaceVariant = MasterdocPalette.Ink2,
    surfaceTint = Color.Transparent,
    outline = MasterdocPalette.Rule,
    outlineVariant = MasterdocPalette.Rule2,
    error = MasterdocPalette.WarmRed,
    onError = MasterdocPalette.Paper,
    errorContainer = MasterdocLiteTokens.WarmRedDim,
    onErrorContainer = MasterdocPalette.WarmRed,
    inverseSurface = MasterdocPalette.Ink,
    inverseOnSurface = MasterdocPalette.Paper,
    inversePrimary = MasterdocPalette.Paper2,
)

val LocalMasterdocFontFamilies = staticCompositionLocalOf<MasterdocFontFamilies> {
    error("MasterdocFontFamilies not provided — wrap content in MasterdocTheme")
}

@Composable
fun MasterdocTheme(content: @Composable () -> Unit) {
    val fonts = rememberMasterdocFontFamilies()
    CompositionLocalProvider(LocalMasterdocFontFamilies provides fonts) {
        MaterialTheme(
            colorScheme = LiteColorScheme,
            typography = masterdocTypography(fonts),
            shapes = masterdocShapes(),
            content = content,
        )
    }
}

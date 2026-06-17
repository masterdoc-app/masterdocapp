package pro.fixaverse.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LiteColorScheme = lightColorScheme(
    primary = FixaversePalette.Ink,
    onPrimary = FixaversePalette.Paper,
    primaryContainer = FixaversePalette.Paper2,
    onPrimaryContainer = FixaversePalette.Ink,
    secondary = FixaversePalette.Flare,
    onSecondary = FixaversePalette.Paper,
    secondaryContainer = FixaversePalette.FlareSoft,
    onSecondaryContainer = FixaversePalette.Flare,
    tertiary = FixaversePalette.Marian,
    onTertiary = FixaversePalette.Paper,
    tertiaryContainer = FixaversePalette.ForestSoft,
    onTertiaryContainer = FixaversePalette.Forest,
    background = FixaversePalette.Paper,
    onBackground = FixaversePalette.Ink,
    surface = FixaversePalette.Paper,
    onSurface = FixaversePalette.Ink,
    surfaceVariant = FixaversePalette.Paper2,
    onSurfaceVariant = FixaversePalette.Ink2,
    surfaceTint = Color.Transparent,
    outline = FixaversePalette.Rule,
    outlineVariant = FixaversePalette.Rule2,
    error = FixaversePalette.WarmRed,
    onError = FixaversePalette.Paper,
    errorContainer = FixaverseLiteTokens.WarmRedDim,
    onErrorContainer = FixaversePalette.WarmRed,
    inverseSurface = FixaversePalette.Ink,
    inverseOnSurface = FixaversePalette.Paper,
    inversePrimary = FixaversePalette.Paper2,
)

val LocalFixaverseFontFamilies = staticCompositionLocalOf<FixaverseFontFamilies> {
    error("FixaverseFontFamilies not provided — wrap content in FixaverseTheme")
}

@Composable
fun FixaverseTheme(content: @Composable () -> Unit) {
    val fonts = rememberFixaverseFontFamilies()
    CompositionLocalProvider(LocalFixaverseFontFamilies provides fonts) {
        MaterialTheme(
            colorScheme = LiteColorScheme,
            typography = fixaverseTypography(fonts),
            shapes = fixaverseShapes(),
            content = content,
        )
    }
}

package pro.masterdoc.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = MasterdocPalette.Ink100,
    onPrimary = MasterdocPalette.Grey00,
    primaryContainer = MasterdocPalette.Grey06,
    onPrimaryContainer = MasterdocPalette.Ink95,
    secondary = MasterdocPalette.Blue50,
    onSecondary = MasterdocPalette.Grey00,
    secondaryContainer = MasterdocPalette.Blue05,
    onSecondaryContainer = MasterdocPalette.Blue40,
    tertiary = MasterdocPalette.Ink90,
    onTertiary = MasterdocPalette.Grey00,
    tertiaryContainer = MasterdocPalette.Stone05,
    onTertiaryContainer = MasterdocPalette.Ink90,
    background = MasterdocPalette.Stone02,
    onBackground = MasterdocPalette.Ink95,
    surface = MasterdocPalette.Grey00,
    onSurface = MasterdocPalette.Ink95,
    surfaceVariant = MasterdocPalette.Stone05,
    onSurfaceVariant = MasterdocPalette.TextSecondary,
    surfaceTint = Color.Transparent,
    outline = MasterdocPalette.Grey10,
    outlineVariant = MasterdocPalette.Grey06,
    error = MasterdocPalette.Red50,
    onError = MasterdocPalette.Grey00,
    errorContainer = MasterdocPalette.Red05,
    onErrorContainer = MasterdocPalette.Red50,
    inverseSurface = MasterdocPalette.Ink100,
    inverseOnSurface = MasterdocPalette.Grey00,
    inversePrimary = MasterdocPalette.Grey20,
)

@Composable
fun MasterdocTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = masterdocTypography(),
        shapes = masterdocShapes(),
        content = content,
    )
}

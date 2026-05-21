package pro.masterdoc.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val White = Color(0xFFFFFFFF)
private val LightGray = Color(0xFFF5F5F5)
private val OnSurface = Color(0xFF1A1A1A)
private val Muted = Color(0xFF616161)

private val LightColors = lightColorScheme(
    primary = OnSurface,
    onPrimary = White,
    primaryContainer = LightGray,
    onPrimaryContainer = OnSurface,
    secondary = Muted,
    onSecondary = White,
    secondaryContainer = LightGray,
    onSecondaryContainer = OnSurface,
    tertiary = Muted,
    onTertiary = White,
    tertiaryContainer = LightGray,
    onTertiaryContainer = OnSurface,
    background = White,
    onBackground = OnSurface,
    surface = White,
    onSurface = OnSurface,
    surfaceVariant = LightGray,
    onSurfaceVariant = Muted,
    surfaceTint = Color.Transparent,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB00020),
)

@Composable
fun MasterdocTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}

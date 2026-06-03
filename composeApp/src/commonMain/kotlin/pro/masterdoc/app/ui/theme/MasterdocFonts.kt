package pro.masterdoc.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import masterdoc.composeapp.generated.resources.Res
import masterdoc.composeapp.generated.resources.fraunces_medium_italic
import masterdoc.composeapp.generated.resources.ibm_plex_sans
import masterdoc.composeapp.generated.resources.jetbrains_mono
import org.jetbrains.compose.resources.Font

data class MasterdocFontFamilies(
    val sans: FontFamily,
    val serif: FontFamily,
    val mono: FontFamily,
)

@Composable
expect fun rememberMasterdocFontFamilies(): MasterdocFontFamilies

@Composable
internal fun rememberResourceFontFamilies(): MasterdocFontFamilies {
    val sans = FontFamily(
        Font(Res.font.ibm_plex_sans, weight = FontWeight.Light),
        Font(Res.font.ibm_plex_sans, weight = FontWeight.Normal),
        Font(Res.font.ibm_plex_sans, weight = FontWeight.Medium),
        Font(Res.font.ibm_plex_sans, weight = FontWeight.SemiBold),
        Font(Res.font.ibm_plex_sans, weight = FontWeight.Bold),
    )
    val serif = FontFamily(
        Font(Res.font.fraunces_medium_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
        Font(Res.font.fraunces_medium_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
        Font(Res.font.fraunces_medium_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    )
    val mono = FontFamily(
        Font(Res.font.jetbrains_mono, weight = FontWeight.Normal),
        Font(Res.font.jetbrains_mono, weight = FontWeight.Medium),
        Font(Res.font.jetbrains_mono, weight = FontWeight.SemiBold),
    )
    return remember(sans, serif, mono) {
        MasterdocFontFamilies(sans = sans, serif = serif, mono = mono)
    }
}

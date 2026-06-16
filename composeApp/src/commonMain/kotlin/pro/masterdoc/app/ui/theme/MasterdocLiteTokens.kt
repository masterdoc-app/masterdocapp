package pro.masterdoc.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design tokens aligned with [fixaverse.ru](https://fixaverse.ru) copilot UI
 * (`masterdoc-toir/landing/copilot-floor.css`, `styles.css`).
 * Clean white surfaces, navy text, blue accent.
 */
object MasterdocLiteTokens {
    // Surfaces
    val Paper = Color(0xFFFFFFFF)
    val Paper2 = Color(0xFFF9FAFB)
    val Paper3 = Color(0xFFEEF3FF)
    val Paper4 = Color(0xFFDBE8FF)

    // Borders
    val Rule = Color(0xFFE5E7EB)
    val Rule2 = Color(0xFFC7D8F5)

    // Text (navy scale)
    val Ink = Color(0xFF0D1B3A)
    val Ink2 = Color(0xFF334155)
    val Ink3 = Color(0xFF64748B)

    // Accent (Fixaverse blue)
    val Flare = Color(0xFF1A6FFF)
    val FlareDim = Color(0x171A6FFF)
    val FlareBorder = Color(0x591A6FFF)
    val FlareTint = Color(0xFFEEF3FF)
    val FlareSoft = Color(0xFFDBE8FF)

    // Status
    val Forest = Color(0xFF16A34A)
    val ForestDim = Color(0x1F16A34A)
    val ForestSoft = Color(0xFFECFDF5)
    val WarmRed = Color(0xFFDC2626)
    val WarmRedDim = Color(0x14DC2626)
    val Marian = Color(0xFF0D1B3A)

    val InkDark = Ink
    val OnPaper = Paper

    val PhoneChrome = Color(0xFF0D1B3A)
    val QrBackdrop = Color(0xFF0A0A0A)
}

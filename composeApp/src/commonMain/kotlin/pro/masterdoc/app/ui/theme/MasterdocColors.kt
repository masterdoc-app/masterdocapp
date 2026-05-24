package pro.masterdoc.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Masterdoc palette — inspired by Onyx (stone tint, ink primary, blue links).
 * @see https://github.com/onyx-dot-app/onyx (colors.css)
 */
object MasterdocPalette {
    // Grey (Onyx grey scale)
    val Grey100 = Color(0xFF000000)
    val Grey90 = Color(0xFF1A1A1A)
    val Grey80 = Color(0xFF333333)
    val Grey50 = Color(0xFF808080)
    val Grey40 = Color(0xFFA4A4A4)
    val Grey20 = Color(0xFFCCCCCC)
    val Grey10 = Color(0xFFE6E6E6)
    val Grey06 = Color(0xFFF0F0F0)
    val Grey04 = Color(0xFFF5F5F5)
    val Grey02 = Color(0xFFFAFAFA)
    val Grey00 = Color(0xFFFFFFFF)

    // Stone tint (Onyx sidebar / app chrome)
    val Stone10 = Color(0xFFE6E6E9)
    val Stone05 = Color(0xFFF0F0F1)
    val Stone02 = Color(0xFFFAFAFA)

    // Onyx brand ink
    val Ink100 = Color(0xFF000000)
    val Ink95 = Color(0xFF1C1C1C)
    val Ink90 = Color(0xFF333333)

    // Action blue
    val Blue50 = Color(0xFF286DF8)
    val Blue40 = Color(0xFF508AFB)
    val Blue10 = Color(0xFFCDDFFF)
    val Blue05 = Color(0xFFE7EFFC)
    val Blue01 = Color(0xFFF8FAFE)

    // Status
    val Red50 = Color(0xFFDC2626)
    val Red10 = Color(0xFFFED2CC)
    val Red05 = Color(0xFFFCEAE7)
    val Green50 = Color(0xFF00A43F)
    val Green10 = Color(0xFFC9E8CC)

    // Text on light (alpha black)
    val TextPrimary = Color(0xE6000000) // ~90%
    val TextSecondary = Color(0x8C000000) // ~55%
    val TextTertiary = Color(0x73000000) // ~45%
}

/** Semantic tokens for UI (light theme). */
object MasterdocColors {
    val AppBackground = MasterdocPalette.Stone02
    val Surface = MasterdocPalette.Grey00
    val SurfaceMuted = MasterdocPalette.Grey02
    val SurfaceTint = MasterdocPalette.Stone05
    val Border = MasterdocPalette.Grey10
    val BorderStrong = MasterdocPalette.Grey20

    val TextPrimary = MasterdocPalette.TextPrimary
    val TextSecondary = MasterdocPalette.TextSecondary
    val TextTertiary = MasterdocPalette.TextTertiary

    val Accent = MasterdocPalette.Blue50
    val AccentMuted = MasterdocPalette.Blue05
    val OnAccent = MasterdocPalette.Grey00

    val UserBubble = MasterdocPalette.Stone05
    val AssistantBubble = MasterdocPalette.Grey00
    val TimelineActive = MasterdocPalette.Blue50
    val TimelineDone = MasterdocPalette.Green50
    val TimelineError = MasterdocPalette.Red50
}

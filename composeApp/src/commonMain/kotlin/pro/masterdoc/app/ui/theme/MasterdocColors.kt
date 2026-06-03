package pro.masterdoc.app.ui.theme

import androidx.compose.ui.graphics.Color

/** Raw palette — lite.masterdoc.pro paper / ink / flare. */
object MasterdocPalette {
    val Paper = MasterdocLiteTokens.Paper
    val Paper2 = MasterdocLiteTokens.Paper2
    val Paper3 = MasterdocLiteTokens.Paper3
    val Paper4 = MasterdocLiteTokens.Paper4
    val Rule = MasterdocLiteTokens.Rule
    val Rule2 = MasterdocLiteTokens.Rule2
    val Ink = MasterdocLiteTokens.Ink
    val Ink2 = MasterdocLiteTokens.Ink2
    val Ink3 = MasterdocLiteTokens.Ink3
    val Flare = MasterdocLiteTokens.Flare
    val FlareSoft = MasterdocLiteTokens.FlareSoft
    val FlareTint = MasterdocLiteTokens.FlareTint
    val Forest = MasterdocLiteTokens.Forest
    val ForestSoft = MasterdocLiteTokens.ForestSoft
    val WarmRed = MasterdocLiteTokens.WarmRed
    val Marian = MasterdocLiteTokens.Marian

    // Legacy aliases (Onyx-era names) → lite tokens
    val Grey00 = Paper
    val Grey02 = Paper2
    val Grey06 = Paper2
    val Grey10 = Rule
    val Grey20 = Rule2
    val Stone02 = Paper
    val Stone05 = Paper2
    val Ink95 = Ink
    val Ink90 = Ink2
    val Ink100 = Ink
    val Blue50 = Flare
    val Blue05 = FlareSoft
    val Blue40 = Flare
    val Red50 = WarmRed
    val Red05 = WarmRed.copy(alpha = 0.08f)
    val Green50 = Forest
    val TextPrimary = Ink
    val TextSecondary = Ink2
    val TextTertiary = Ink3
}

/** Semantic tokens for Compose UI. */
object MasterdocColors {
    val AppBackground = MasterdocPalette.Paper
    val Surface = MasterdocPalette.Paper
    val SurfaceElevated = MasterdocPalette.Paper2
    val SurfaceMuted = MasterdocPalette.Paper3
    val SurfaceTint = MasterdocPalette.Paper2
    val Border = MasterdocPalette.Rule
    val BorderStrong = MasterdocPalette.Rule2

    val TextPrimary = MasterdocPalette.Ink
    val TextSecondary = MasterdocPalette.Ink2
    val TextTertiary = MasterdocPalette.Ink3

    val Accent = MasterdocPalette.Flare
    val AccentMuted = MasterdocPalette.FlareSoft
    val AccentTint = MasterdocLiteTokens.FlareTint
    val OnAccent = MasterdocPalette.Paper

    val UserBubble = MasterdocPalette.Ink
    val UserBubbleContent = MasterdocPalette.Paper
    val AssistantBubble = MasterdocPalette.Paper
    val TimelineActive = MasterdocPalette.Flare
    val TimelineDone = MasterdocPalette.Forest
    val TimelineError = MasterdocPalette.WarmRed

    val Success = MasterdocPalette.Forest
    val SuccessMuted = MasterdocPalette.ForestSoft
}

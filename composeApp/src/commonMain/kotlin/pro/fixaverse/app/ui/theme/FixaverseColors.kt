package pro.fixaverse.app.ui.theme

import androidx.compose.ui.graphics.Color
import pro.fixaverse.design.theme.FixaverseLiteTokens

/** Raw palette — Fixaverse copilot (fixaverse.ru). */
object FixaversePalette {
    val Paper = FixaverseLiteTokens.Paper
    val Paper2 = FixaverseLiteTokens.Paper2
    val Paper3 = FixaverseLiteTokens.Paper3
    val Paper4 = FixaverseLiteTokens.Paper4
    val Rule = FixaverseLiteTokens.Rule
    val Rule2 = FixaverseLiteTokens.Rule2
    val Ink = FixaverseLiteTokens.Ink
    val Ink2 = FixaverseLiteTokens.Ink2
    val Ink3 = FixaverseLiteTokens.Ink3
    val Flare = FixaverseLiteTokens.Flare
    val FlareSoft = FixaverseLiteTokens.FlareSoft
    val FlareTint = FixaverseLiteTokens.FlareTint
    val Forest = FixaverseLiteTokens.Forest
    val ForestSoft = FixaverseLiteTokens.ForestSoft
    val WarmRed = FixaverseLiteTokens.WarmRed
    val Marian = FixaverseLiteTokens.Marian

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
object FixaverseColors {
    val AppBackground = FixaversePalette.Paper
    val Surface = FixaversePalette.Paper
    val SurfaceElevated = FixaversePalette.Paper2
    val SurfaceMuted = FixaversePalette.Paper3
    val SurfaceTint = FixaversePalette.Paper2
    val Border = FixaversePalette.Rule
    val BorderStrong = FixaversePalette.Rule2

    val TextPrimary = FixaversePalette.Ink
    val TextSecondary = FixaversePalette.Ink2
    val TextTertiary = FixaversePalette.Ink3

    val Accent = FixaversePalette.Flare
    val AccentMuted = FixaversePalette.FlareSoft
    val AccentTint = FixaverseLiteTokens.FlareTint
    val OnAccent = FixaversePalette.Paper

    val UserBubble = FixaversePalette.Ink
    val UserBubbleContent = FixaversePalette.Paper
    val AssistantBubble = FixaversePalette.Paper
    val TimelineActive = FixaversePalette.Flare
    val TimelineDone = FixaversePalette.Forest
    val TimelineError = FixaversePalette.WarmRed

    val Success = FixaversePalette.Forest
    val SuccessMuted = FixaversePalette.ForestSoft
}

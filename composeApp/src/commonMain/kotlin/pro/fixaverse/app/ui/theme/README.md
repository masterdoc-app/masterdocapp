# Masterdoc Design System (Fixaverse)

Aligned with **[fixaverse.ru](https://fixaverse.ru)** — white / navy / blue palette, IBM Plex Sans, Fraunces, JetBrains Mono.

Source of truth for CSS tokens: `masterdoc-toir/landing/copilot-floor.css` and `styles.css`.

## Palette

| Token | Hex | Usage |
|-------|-----|--------|
| Paper | `#FFFFFF` | App background, assistant bubble |
| Paper2 | `#F9FAFB` | Elevated surfaces, app-head, input bar |
| Paper3 | `#EEF3FF` | Accent tint surfaces |
| Rule | `#E5E7EB` | Borders |
| Rule2 | `#C7D8F5` | Strong borders, home bar |
| Ink | `#0D1B3A` | Primary text, user bubble, primary button |
| Ink2 | `#334155` | Secondary text |
| Ink3 | `#64748B` | Hints, timestamps |
| Flare | `#1A6FFF` | Accent, mic, links, mono labels |
| Flare soft | `#DBE8FF` | Option letter badge |
| Forest | `#16A34A` | Success, live indicator |
| Marian | `#0D1B3A` | Brand mark, phone chrome |

## Typography

| Role | Font | Example |
|------|------|---------|
| Marketing / emphasis | Fraunces italic | `Masterdoc` in hero |
| UI body | IBM Plex Sans | Bubbles, buttons |
| Labels / meta | JetBrains Mono uppercase | `SMT-12 · ЛИНИЯ 3` |

Fonts live in `composeApp/src/commonMain/composeResources/font/`.

## Components (copilot wireframes)

| Composable | CSS |
|------------|----------|
| `LiteAppHead` | `.app-head` |
| `MasterdocMessageSurface` | `.bubble.user` / `.bubble.mary` |
| `LiteOptionCard` | `.opt` |
| `LiteChip` | `.chip` |
| `LiteFlareButton` | `.btn` with accent arrow |
| `MasterdocPrimaryButton` | `.btn` navy fill |
| `Modifier.fixaverseConvoBackground()` | `.convo` gradient |

## Usage

```kotlin
MasterdocTheme {
    val fonts = LocalMasterdocFontFamilies.current
    LiteAppHead(title = "Fixaverse · SMT-12", subtitle = "Голос · активен", subtitleLive = true)
    LiteOptionCard(letter = "A", body = "Сброс — удерживайте START 3 сек…")
    LiteChip(text = "82% уверенность · 11 похожих", flare = true)
}
```

Prefer `MaterialTheme.colorScheme` in screens; use `MasterdocLiteTokens` / `MasterdocColors` for docs and Figma parity.

## Files

- `MasterdocLiteTokens.kt` — raw colors
- `MasterdocColors.kt` — semantic tokens
- `MasterdocFonts.kt` — bundled font families
- `MasterdocTypography.kt` — M3 scale + `MasterdocLiteTextStyles`
- `MasterdocDimens.kt` — spacing, radii
- `MasterdocShapes.kt` — bubbles, square buttons
- `MasterdocComponents.kt` — UI building blocks
- `Theme.kt` — `MasterdocTheme()`

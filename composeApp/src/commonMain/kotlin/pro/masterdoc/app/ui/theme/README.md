# Masterdoc Design System (lite)

Aligned with **[lite.masterdoc.pro](https://lite.masterdoc.pro)** — paper / ink / flare palette, IBM Plex Sans, Fraunces, JetBrains Mono.

Source of truth for CSS tokens: `masterdoc-lite/landing/index.html` (`:root` variables).

## Palette

| Token | Hex | Usage |
|-------|-----|--------|
| Paper | `#FBF8F3` | App background, assistant bubble |
| Paper2 | `#F3EEE4` | Elevated surfaces, app-head, input bar |
| Paper3 | `#EAE3D4` | Section breaks |
| Rule | `#DDD6C7` | Borders |
| Rule2 | `#C8BFA9` | Strong borders |
| Ink | `#1A1814` | Primary text, user bubble, primary button |
| Ink2 | `#3A3530` | Secondary text |
| Ink3 | `#6B665C` | Hints, timestamps |
| Flare | `#C2410C` | Accent, mic, links, mono labels |
| Flare soft | `#F3E4D2` | Option letter badge |
| Forest | `#2E6B3E` | Success, live indicator |
| Marian | `#2A3E6B` | Brand mark on fault screen |

## Typography

| Role | Font | Example |
|------|------|---------|
| Marketing / emphasis | Fraunces italic | `Masterdoc` in hero |
| UI body | IBM Plex Sans | Bubbles, buttons |
| Labels / meta | JetBrains Mono uppercase | `SMT-12 · ЛИНИЯ 3` |

Fonts live in `composeApp/src/commonMain/composeResources/font/`.

## Components (lite wireframes)

| Composable | lite CSS |
|------------|----------|
| `LiteAppHead` | `.app-head` |
| `MasterdocMessageSurface` | `.bubble.user` / `.bubble.mary` |
| `LiteOptionCard` | `.opt` |
| `LiteChip` | `.chip` |
| `LiteFlareButton` | `.btn` with flare arrow |
| `MasterdocPrimaryButton` | `.btn` ink fill |
| `Modifier.masterdocConvoBackground()` | `.convo` gradient |

## Usage

```kotlin
MasterdocTheme {
    val fonts = LocalMasterdocFontFamilies.current
    LiteAppHead(title = "Masterdoc · SMT-12", subtitle = "Голос · активен", subtitleLive = true)
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
- `MasterdocDimens.kt` — spacing, lite radii
- `MasterdocShapes.kt` — bubbles, square buttons
- `MasterdocComponents.kt` — UI building blocks
- `Theme.kt` — `MasterdocTheme()`

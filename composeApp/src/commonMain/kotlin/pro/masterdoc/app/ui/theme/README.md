# Masterdoc Design System

Light theme inspired by [Onyx](https://github.com/onyx-dot-app/onyx) (`colors.css`): warm stone backgrounds, ink primary, blue accents.

## Tokens

| Token | Hex | Onyx reference |
|-------|-----|----------------|
| App background | `#FAFAFA` | `background-tint-01` / stone-02 |
| Surface | `#FFFFFF` | `background-neutral-00` |
| Surface tint | `#F0F0F1` | `background-tint-02` / stone-05 |
| Primary (ink) | `#000000` | `theme-primary-05` |
| Accent (links, CTA) | `#286DF8` | `action-link-05` |
| Border | `#E6E6E6` | `border-01` |
| Text primary | 90% black | `text-05` |
| Text secondary | 55% black | `text-03` |

## Files

- `MasterdocColors.kt` — palette + semantic colors
- `MasterdocDimens.kt` — spacing, radii (8 / 12 / 16 dp)
- `MasterdocTypography.kt` — type scale
- `MasterdocShapes.kt` — M3 shapes + chat bubble corners
- `MasterdocComponents.kt` — cards, buttons, message surfaces
- `Theme.kt` — `MasterdocTheme()` wiring

## Usage

```kotlin
MasterdocTheme {
    MasterdocSelectableCard(title = "…", onClick = { })
}
```

Prefer `MaterialTheme.colorScheme` in composables; use `MasterdocColors` for docs and non-Compose tooling.

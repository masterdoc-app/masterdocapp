# Design theme dependency (copilot / masterdocapp)

> Проверено: 2026-07-25 · источники: сессия

## Вердикт

Copilot (`masterdocapp`) использует shared Lite theme из репы `fixaverse-design` через Gradle `includeBuild` (соседний клон / CI checkout). Maven GitHub Packages — только fallback с `gpr.key` / `read:packages`.

**С чего начать:** клонировать `fixaverse-design` рядом с `masterdocapp`.

## Факты

| Факт | Статус |
|------|--------|
| Dep: `pro.fixaverse:design-theme:0.1.0` | ✅ |
| Resolve: `../fixaverse-design` или `./fixaverse-design` → project `:theme` | ✅ |
| Подробности | https://github.com/masterdoc-app/fixaverse-design/blob/main/docs/consumer-resolution.md (после push) / локально `../fixaverse-design/docs/consumer-resolution.md` |

## История правок

| Дата | Что изменили |
|------|----------------|
| 2026-07-25 | первичная запись |

# Тесты Masterdoc App

## Локально (всегда)

```bash
./gradlew check
```

Включает unit-тесты JVM/desktop и компиляцию всех таргетов (см. [AGENTS.md](../AGENTS.md)).

## Integration-тесты API (опционально)

Запросы на продакшен API. **Не** входят в CI по умолчанию.

```bash
export MASTERDOC_INTEGRATION=1
# опционально: export MASTERDOC_API_BASE_URL=https://api.masterdoc.pro/v1

./gradlew :shared:jvmTest --tests "pro.masterdoc.data.assistant.*"
./gradlew :shared:jvmTest --tests "pro.masterdoc.data.chat.*"
```

| Класс | Что проверяет |
|-------|----------------|
| `MasterdocAssistantsApiIntegrationTest` | `GET /v1/assistants` — непустой список |
| `DetectAssistantIntegrationTest` | `POST /v1/assistants/detect` — JPEG-стаб → имя станции из списка assistants (до ~5 мин, LLM) |
| `MasterdocChatApiIntegrationTest` | `POST` чат — ответ ассистента |
| `StreamingChatIntegrationTest` | Стриминг чата + timeline |

Проверенный сценарий detect (браузер, 2026-06): снимок с `getUserMedia` → `POST …/assistants/detect` → **200** → переход на «Описание сбоя». Зафиксирован в `DetectAssistantIntegrationTest` (стаб `detect-integration-stub.jpg`).

Если integration падает с `500` и `Request timeout` от Onyx — повторите позже или проверьте бэкенд; тест делает до 2 попыток.

## Unit-тесты (JVM, без сети)

| Класс | Что проверяет |
|-------|----------------|
| `MockChatRepositoryTest` | Mock-репозиторий чата |
| `OnyxStreamAccumulatorTest` | Парсинг NDJSON/SSE стрима |
| `DefaultRootComponentBackTest` | `onBack()` / стек навигации |

## UI-тесты (Desktop)

| Класс | Что проверяет |
|-------|----------------|
| `LiteAppHeadBackTest` | Кнопка «Назад» в `LiteAppHead` (`MasterdocTestTags.APP_HEAD_BACK`) |
| `MasterdocDetectLoadingOverlayTest` | Оверлей «Распознаём станцию…» при `isDetecting` (`MasterdocTestTags.DETECT_LOADING_OVERLAY`) |

## Ручная проверка (Web + камера)

См. [IMAGE_PICKER.md](IMAGE_PICKER.md):

```bash
./scripts/run-web.sh
# http://127.0.0.1:8088/ — hard refresh, masterdoc-camera.js?v=32+
```

Чеклист: «Сканировать» → live preview (не «Открыть файл») → снимок → оверлей «Распознаём станцию…» (до ответа API) → экран «Описание сбоя».

## Backend (отдельный модуль)

В `backend/src/test/kotlin/`: парсер detect, Onyx mapping, send chat request и др. Запуск: `./gradlew :backend:test` из корня backend-репозитория (если подключён).

## CI (GitHub Actions)

См. `.github/workflows/ci.yml` — сборка Android, Desktop, Wasm, iOS. **Без** `MASTERDOC_INTEGRATION` (без сетевых integration-тестов).

В [COPILOT_SPEC.md](COPILOT_SPEC.md) упомянуты integration tests чата — тот же флаг `MASTERDOC_INTEGRATION=1`.

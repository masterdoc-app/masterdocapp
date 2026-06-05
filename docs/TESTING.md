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
| `RefrigeratorFullFlowE2eTest` | **Полный lite-flow UI (Desktop):** Scan → Холодильники → вопрос в чат → ответ Onyx → итог → `POST /v1/report` → проверка `GET /v1/report`; опционально grep логов бэкенда |
| `KodioRecordingDesktopTest` | Kodio: запись ~0.8 с → WAV (нужен микрофон + `MASTERDOC_INTEGRATION=1`) |

### Полный E2E (Desktop UI + API + логи)

```bash
export MASTERDOC_INTEGRATION=1
# опционально: export MASTERDOC_API_BASE_URL=https://api.masterdoc.pro/v1

./gradlew :composeApp:desktopTest --tests RefrigeratorFullFlowE2eTest
```

### Голос (Kodio + Onyx STT)

На **Desktop/Android** экран «Опишите»: микрофон → Kodio (`rememberRecorderState`) → WAV → `POST /v1/voice/transcribe` → текст в чат.

```bash
export MASTERDOC_INTEGRATION=1
./gradlew :composeApp:desktopTest --tests KodioRecordingDesktopTest
```

На **Web (Wasm)** и **iOS** пока заглушка — текстовый ввод.

Требования: в Onyx Admin включён STT (Voice Mode), на бэкенде задеплоен `POST /v1/voice/transcribe`.

Сценарий (автоматически):

1. Запуск `RootContent` (Desktop UI test).
2. «Список оборудования» → выбор станции с именем **Холодильник**\*.
3. «Ввести текстом» → «холодильник не включается» → «Отправить».
4. Ожидание ответа ассистента (до **10 мин**).
5. «Завершить кейс» → поле результата с маркером `e2e-flow-<timestamp>` → «Отправить».
6. **БД:** `GET /v1/report?assistant_id=…` содержит маркер (тот же SQLite, что на сервере).
7. **Логи (опционально):** если задан `MASTERDOC_E2E_SERVER_LOG_CMD`, тест ищет строку `[masterdoc case-report] saved …` с маркером. Или вручную:

```bash
chmod +x scripts/verify-e2e-report-log.sh
export MASTERDOC_E2E_SERVER_LOG_CMD='ssh root@YOUR_HOST journalctl -u masterdoc-backend --since "20 min ago"'
./scripts/verify-e2e-report-log.sh e2e-flow-1717420000000
```

\* В API имя может быть «Холодильники» — тест ищет подстроку `Холодильник`.

Проверенный сценарий detect (браузер, 2026-06): снимок с `getUserMedia` → `POST …/assistants/detect` → **200** → переход на «Описание сбоя». Зафиксирован в `DetectAssistantIntegrationTest` (стаб `detect-integration-stub.jpg`).

Если integration падает с `500` и `Request timeout` от Onyx — проверьте Onyx/LLM; бэкенд и nginx ждут detect до **600 с**, клиент (`HttpClientFactory`) — тоже. Тест делает до 2 попыток.

## Unit-тесты (JVM, без сети)

| Класс | Что проверяет |
|-------|----------------|
| `MockChatRepositoryTest` | Mock-репозиторий чата |
| `OnyxStreamAccumulatorTest` | Парсинг NDJSON/SSE стрима |
| `DefaultRootComponentBackTest` | `onBack()` / стек: Scan ↔ Describe ↔ Summary, Camera cancel |

## UI-тесты (Desktop)

| Класс | Что проверяет |
|-------|----------------|
| `LiteAppHeadBackTest` | Кнопка «Назад» в `LiteAppHead` (`MasterdocTestTags.APP_HEAD_BACK`) |
| `MasterdocDetectLoadingOverlayTest` | Оверлей «Распознаём станцию…» (`DETECT_LOADING_OVERLAY`, `DETECT_LOADING_TITLE`) |
| `ScanScreenDetectLoaderTest` | Появление/скрытие оверлея на экране скана при `isDetecting` |
| `FlowScreensBackButtonTest` | Стрелка «Назад» на Scan, списке оборудования, Describe, Summary, Camera |
| `FrequentIssuesScreenTest` | Экран «Частые неисправности»: UI + golden PNG в `composeApp/src/desktopTest/screenshots/golden/` |
| `RefrigeratorFullFlowE2eTest` | Полный lite-flow на прод API (`MASTERDOC_INTEGRATION=1`, см. выше) |

Перегенерация скриншотов экрана частых неисправностей:

```bash
./gradlew :composeApp:desktopTest --tests FrequentIssuesScreenTest
```

Файлы: `frequent_issues_empty.png`, `frequent_issues_list.png`.

## Ручная проверка (Web + камера)

См. [IMAGE_PICKER.md](IMAGE_PICKER.md):

```bash
./scripts/run-web.sh
# http://127.0.0.1:8088/ — hard refresh, composeApp.js?v=27+

# Если после «Serving HTTP…» сразу «Killed» (OOM после Gradle/Webpack):
MASTERDOC_WEB_SKIP_BUILD=1 ./scripts/run-web.sh
# или только сервер:
./scripts/serve-web.sh
```

Чеклист: «Сканировать» → live preview (не «Открыть файл») → снимок → оверлей «Распознаём станцию…» (до ответа API) → экран «Описание сбоя».

## Backend (отдельный модуль)

В `backend/src/test/kotlin/`: парсер detect, Onyx mapping, send chat request и др. Запуск: `./gradlew :backend:test` из корня backend-репозитория (если подключён).

## CI (GitHub Actions)

См. `.github/workflows/ci.yml`:

- **job `test`** — `:shared:jvmTest`, `:composeApp:desktopTest`, компиляция Wasm (без `MASTERDOC_INTEGRATION`);
- остальные jobs — сборка Android, Desktop, Wasm, iOS.

Integration-тесты API в CI **не** запускаются (только локально с `MASTERDOC_INTEGRATION=1`).

В [COPILOT_SPEC.md](COPILOT_SPEC.md) упомянуты integration tests чата — тот же флаг `MASTERDOC_INTEGRATION=1`.

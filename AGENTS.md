# Masterdoc App — правила для агентов и разработчиков

## Локальная проверка — после каждой задачи

После **каждой завершённой задачи** в корне `masterdocapp/`:

```bash
./gradlew check
```

**Не считать задачу выполненной**, пока `./gradlew check` не завершился с `BUILD SUCCESSFUL`.

Опционально API на проде (detect, чат): см. [docs/TESTING.md](docs/TESTING.md) — `MASTERDOC_INTEGRATION=1`.

## CI — обязательно при каждом push и MR

После **каждого push** и перед мержем PR:

1. Откройте вкладку **Actions** в GitHub и дождитесь завершения workflow **CI**.
2. Убедитесь, что зелёные все джобы:
   - **KMP — Android** (`assembleDebug`)
   - **KMP — Desktop** (`compileKotlinDesktop`)
   - **KMP — Web (Wasm)** (`compileKotlinWasmJs`)
   - **KMP — iOS** (`compileKotlinIosSimulatorArm64`, runner `macos-latest`)
3. Локально перед push (по возможности):

```bash
./gradlew :composeApp:assembleDebug :composeApp:compileKotlinDesktop :composeApp:compileKotlinWasmJs
```

На macOS дополнительно:

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

**Не мержить PR**, пока CI не прошёл. **Не считать задачу выполненной**, пока не проверены статусы CI на push.

## Web-деплой (ветка trunk)

Push в **`trunk`** запускает workflow **Deploy Web to VPS** → `https://copilot.masterdoc.pro` (статика на VPS `91.207.75.72`, nginx).

1. Дождаться зелёного **Deploy Web to VPS** в Actions.
2. Smoke (локально или на CI): `./scripts/smoke-copilot-web.sh https://copilot.masterdoc.pro`
3. DNS: см. [deploy/DNS_CUTOVER.md](deploy/DNS_CUTOVER.md) — A-запись `91.207.75.72`, не GitHub Pages.

Секреты: `DEPLOY_SSH_PRIVATE_KEY`, `DEPLOY_USER` (те же, что у lite/toir на том же VPS).

## API (обязательно)

**Всегда** работаем с продакшен API:

```text
http://api.masterdoc.pro/v1
```

- В `local.properties`: `masterdoc.api.baseUrl=http://api.masterdoc.pro/v1` (см. `local.properties.example`).
- После `certbot --nginx` на VPS можно перейти на `https://api.masterdoc.pro/v1`.
- Если ключ пустой, все платформы берут тот же URL из `DEFAULT_API_BASE_URL` в `shared/.../ApiConfig.kt`.
- **Не** переключать на `127.0.0.1` / `10.0.2.2` без явной просьбы пользователя.
- После смены URL в `local.properties` — пересобрать приложение (`./gradlew check` или `./scripts/run-web.sh`).

Проверка:

```bash
curl -fsS http://api.masterdoc.pro/health
curl -fsS http://api.masterdoc.pro/v1/assistants
```

## Стек

- Compose Multiplatform, Decompose, MVIKotlin, Koin, Ktor (REST, без локальной БД в v1)
- `shared/` — логика и компоненты; `composeApp/` — UI и entry points
- **Камера / галерея:** [ImagePickerKMP](https://github.com/ismoy/ImagePickerKMP) `1.0.34` на Android, iOS, Desktop; на **Wasm** — `masterdoc-camera.js` (см. [docs/IMAGE_PICKER.md](docs/IMAGE_PICKER.md))

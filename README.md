# Masterdoc App

Кроссплатформенное приложение для владельцев холодильников **Атлант**: помощник по эксплуатации, диагностике и типовым неисправностям на базе ИИ.

## О продукте

Masterdoc App помогает разобраться с повседневными вопросами по работе холодильника — без долгого поиска в инструкции и без ожидания звонка в сервис. Вы описываете ситуацию текстом, голосом или фото, а приложение подсказывает, что проверить и как действовать дальше.

## Возможности

- **Чат по эксплуатации** — ответы на основные вопросы: настройки, режимы, уход, типичные ситуации в быту.
- **Голос и фото** — можно задать вопрос голосом или приложить снимок (индикаторы, панель, уплотнитель, лёд и т.п.), чтобы получить более точную подсказку.
- **Частые неисправности** — краткие сценарии: что могло произойти, что проверить самостоятельно, когда лучше обратиться в сервис.
- **Правильная эксплуатация** — рекомендации по установке, загрузке, разморозке, уходу и бережному использованию техники.

## Платформы и стек

| Платформа | Целевые системы |
|-----------|-----------------|
| Android | Смартфоны и планшеты |
| iOS | iPhone и iPad |
| Desktop | Windows, macOS, Linux |
| Web | Браузер |

**Kotlin Multiplatform** + **Compose Multiplatform**, **Decompose**, **MVIKotlin**, **Koin**, **Ktor** (REST), **[ImagePickerKMP](https://github.com/ismoy/ImagePickerKMP)** (камера/галерея на native; Wasm — см. [docs/IMAGE_PICKER.md](docs/IMAGE_PICKER.md)).

Структура: `shared/` (логика), `composeApp/` (UI), `iosApp/` (оболочка Xcode).

## API

Все платформы по умолчанию ходят на продакшен:

```text
http://api.masterdoc.pro/v1
```

Настройка: `masterdoc.api.baseUrl` в `local.properties` (см. `local.properties.example` и [AGENTS.md](AGENTS.md)).

## Сборка и запуск

```bash
# Android APK
./gradlew :composeApp:assembleDebug

# Desktop
./gradlew :composeApp:run

# Web (dev server; may need ulimit -n on Linux)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

В **Android Studio** не вставляйте строки с `#` в поле Gradle tasks — Studio передаёт `#` как имя задачи (`Task '#' not found`). Запускайте через конфигурацию **Android App** (модуль `composeApp`) или только задачу `:composeApp:installDebug` без комментариев.

iOS: см. [iosApp/README.md](iosApp/README.md).

## CI и деплой Web

- На каждый push и PR — сборка всех платформ (workflow **CI**, см. [AGENTS.md](AGENTS.md)).
- Push в **`trunk`** — деплой Wasm на **https://copilot.masterdoc.pro** (VPS `91.207.75.72`, workflow **Deploy Web to VPS**).

Секреты GitHub (`Settings` → `Secrets and variables` → `Actions`):

| Секрет | Обязательный | Описание |
|--------|--------------|----------|
| `DEPLOY_SSH_PRIVATE_KEY` | да | SSH-ключ (ed25519), публичная часть в `authorized_keys` на VPS |
| `DEPLOY_USER` | да | Пользователь SSH, например `root` |
| `CERTBOT_EMAIL` | нет | Email для Let's Encrypt; по умолчанию `admin@masterdoc.pro` |

Smoke после деплоя: `./scripts/smoke-copilot-web.sh`

Переключение DNS с GitHub Pages: [deploy/DNS_CUTOVER.md](deploy/DNS_CUTOVER.md).

## Связанные репозитории

| Репозиторий | Назначение |
|-------------|------------|
| [masterdoc](https://github.com/masterdoc-app/masterdoc) | Лендинг |
| [masterdocapp](https://github.com/masterdoc-app/masterdocapp) | Приложение (этот репозиторий) |

## Лицензия

Уточняется.

# Камера и галерея (ImagePickerKMP)

## Библиотека

[ImagePickerKMP](https://github.com/ismoy/ImagePickerKMP) — единый API камеры и галереи для KMP.

```kotlin
// gradle/libs.versions.toml
imagePickerKmp = "1.0.34"  // совместима с Kotlin 2.1.x проекта
```

В `composeApp/build.gradle.kts`:

- зафиксирован `androidx.activity:activity-compose` на версии проекта (1.10.1), чтобы транзитив ImagePickerKMP не требовал compileSdk 36;
- отключены задачи `lint*` (анализатор падает на связке ImagePickerKMP + Compose — `NonNullableMutableLiveDataDetector`).

Версии **≥ 1.0.38** собраны под Kotlin **2.3.20** — для Masterdoc пока используем **1.0.34**.

Документация upstream: https://imagepickerkmp.dev/

## Интеграция в Masterdoc

| Платформа | Реализация |
|-----------|------------|
| Android | `ImagePickerLauncher` / `GalleryPickerLauncher` ([ImagePickerKmpHosts.kt](../composeApp/src/commonMain/kotlin/pro/masterdoc/app/platform/ImagePickerKmpHosts.kt)) |
| iOS | то же |
| Desktop (JVM) | то же |
| **Web (Wasm)** | **не** через UI библиотеки (wasmJs в ImagePickerKMP — заглушка). Камера: `masterdoc-camera.js` + `getUserMedia`, см. [ImagePicker.wasmJs.kt](../composeApp/src/wasmJsMain/kotlin/pro/masterdoc/app/platform/ImagePicker.wasmJs.kt) |

Приложение вызывает один API:

```kotlin
val pickers = rememberImagePickerLaunchers(
    onResult = { picked: PickedImage? -> ... },
    onCameraError = { message -> ... },
)
pickers.openCamera()   // live preview / native camera
pickers.openGallery()
```

### Wasm: trusted gesture

Клик по Compose canvas **не** даёт браузеру «доверенный» жест для `getUserMedia`. На экране скана:

1. `ScanScreenCameraBindings` регистрирует колбэки в `masterdocActivateScanCamera`.
2. Поверх кнопки — HTML `#masterdoc-scan-shutter` ([index.html](../composeApp/src/wasmJsMain/resources/index.html)).
3. `openCamera()` на Wasm вызывает `masterdocOpenScanCamera()` (live preview, без `<input type="file">`).

Локальная проверка:

```bash
./scripts/run-web.sh
# http://127.0.0.1:8088/ — hard refresh после смены ?v= у masterdoc-camera.js
```

Автотест detect API: [TESTING.md](TESTING.md) → `DetectAssistantIntegrationTest`.

### Конфигурация камеры

`skipConfirmation = true`, `enableCrop = false` — сразу отдаём JPEG/PNG в detect API ([ImagePickerKmpSupport.kt](../composeApp/src/commonMain/kotlin/pro/masterdoc/app/platform/ImagePickerKmpSupport.kt)).

## Обновление библиотеки

1. Проверить [CHANGELOG](https://github.com/ismoy/ImagePickerKMP/blob/main/docs/CHANGELOG.md) на Kotlin ABI.
2. Если переходите на **1.0.38+**, поднимите Kotlin проекта до **2.3.20** и Compose **1.10.x**.
3. Проверить, появилась ли рабочая реализация `ImagePickerLauncher.wasmJs` (сейчас stub «not supported»).

## iOS

В `iosApp` должен быть `NSCameraUsageDescription` в Info.plist (см. upstream INTEGRATION_GUIDE).

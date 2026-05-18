# Masterdoc App — правила для агентов и разработчиков

## Локальная проверка — после каждой задачи

После **каждой завершённой задачи** в корне `masterdocapp/`:

```bash
./gradlew check
```

**Не считать задачу выполненной**, пока `./gradlew check` не завершился с `BUILD SUCCESSFUL`.

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

## Стек

- Compose Multiplatform, Decompose, MVIKotlin, Koin, Ktor (REST, без локальной БД в v1)
- `shared/` — логика и компоненты; `composeApp/` — UI и entry points

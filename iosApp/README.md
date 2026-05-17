# iOS app

На macOS соберите framework и откройте проект в Xcode:

```bash
cd app
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

Создайте Xcode-проект в этой папке (File → New → App) или используйте [KMP wizard](https://kmp.jetbrains.com/) и подключите `ComposeApp.framework`.

`ContentView.swift` вызывает `MainViewController()` из модуля `composeApp`.

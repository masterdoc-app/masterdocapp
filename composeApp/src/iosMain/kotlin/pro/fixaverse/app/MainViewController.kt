package pro.fixaverse.app

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import pro.fixaverse.app.di.createRootComponent
import pro.fixaverse.app.di.initKoin
import platform.UIKit.UIViewController

private var isKoinStarted = false

private fun ensureKoin() {
    if (!isKoinStarted) {
        initKoin()
        isKoinStarted = true
    }
}

@Suppress("FunctionName")
fun MainViewController(): UIViewController {
    ensureKoin()

    val lifecycle = LifecycleRegistry()
    val root = createRootComponent(DefaultComponentContext(lifecycle = lifecycle))
    lifecycle.onCreate()

    return ComposeUIViewController {
        App(rootComponent = root)
    }
}

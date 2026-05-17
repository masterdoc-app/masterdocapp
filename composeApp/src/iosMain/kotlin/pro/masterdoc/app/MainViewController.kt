package pro.masterdoc.app

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import pro.masterdoc.app.di.createRootComponent
import pro.masterdoc.app.di.initKoin
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

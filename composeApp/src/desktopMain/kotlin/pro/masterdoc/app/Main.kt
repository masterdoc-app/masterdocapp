package pro.masterdoc.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import pro.masterdoc.app.di.createRootComponent
import pro.masterdoc.app.di.initKoin

fun main() = application {
    initKoin()

    val lifecycle = LifecycleRegistry()
    val root = createRootComponent(DefaultComponentContext(lifecycle = lifecycle))
    lifecycle.onCreate()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Masterdoc",
    ) {
        App(rootComponent = root)
    }
}

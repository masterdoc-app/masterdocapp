package pro.fixaverse.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import pro.fixaverse.app.di.createRootComponent
import pro.fixaverse.app.di.initKoin

fun main() = application {
    initKoin()

    val lifecycle = LifecycleRegistry()
    val root = createRootComponent(DefaultComponentContext(lifecycle = lifecycle))
    lifecycle.onCreate()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Fixaverse",
    ) {
        App(rootComponent = root)
    }
}

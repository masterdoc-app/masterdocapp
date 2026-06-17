package pro.fixaverse.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.browser.document
import pro.fixaverse.app.di.createRootComponent
import pro.fixaverse.app.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()

    val lifecycle = LifecycleRegistry()
    val root = createRootComponent(DefaultComponentContext(lifecycle = lifecycle))
    lifecycle.onCreate()

    val viewportRoot =
        document.getElementById("fixaverse-compose-root")
            ?: document.body!!
    ComposeViewport(viewportRoot) {
        App(rootComponent = root)
    }
}

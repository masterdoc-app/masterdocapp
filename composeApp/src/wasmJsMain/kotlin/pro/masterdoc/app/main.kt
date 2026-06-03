package pro.masterdoc.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.browser.document
import pro.masterdoc.app.di.createRootComponent
import pro.masterdoc.app.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()

    val lifecycle = LifecycleRegistry()
    val root = createRootComponent(DefaultComponentContext(lifecycle = lifecycle))
    lifecycle.onCreate()

    val viewportRoot =
        document.getElementById("masterdoc-compose-root")
            ?: document.body!!
    ComposeViewport(viewportRoot) {
        App(rootComponent = root)
    }
}

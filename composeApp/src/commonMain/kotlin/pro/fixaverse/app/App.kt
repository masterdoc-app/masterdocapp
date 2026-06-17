package pro.fixaverse.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pro.fixaverse.app.platform.FixaverseUriHandlerProvider
import pro.fixaverse.app.ui.RootContent
import pro.fixaverse.app.ui.theme.FixaverseTheme
import pro.fixaverse.presentation.root.RootComponent

@Composable
fun App(rootComponent: RootComponent) {
    FixaverseUriHandlerProvider {
        FixaverseTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                RootContent(component = rootComponent)
            }
        }
    }
}

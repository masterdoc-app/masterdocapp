package pro.masterdoc.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pro.masterdoc.app.ui.RootContent
import pro.masterdoc.app.ui.theme.MasterdocTheme
import pro.masterdoc.presentation.root.RootComponent

@Composable
fun App(rootComponent: RootComponent) {
    MasterdocTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            RootContent(component = rootComponent)
        }
    }
}

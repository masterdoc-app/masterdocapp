package pro.masterdoc.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler

@Composable
expect fun rememberMasterdocUriHandler(): UriHandler

@Composable
fun MasterdocUriHandlerProvider(content: @Composable () -> Unit) {
    val handler = rememberMasterdocUriHandler()
    CompositionLocalProvider(LocalUriHandler provides handler) {
        content()
    }
}

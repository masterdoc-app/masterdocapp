package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler

@Composable
expect fun rememberFixaverseUriHandler(): UriHandler

@Composable
fun FixaverseUriHandlerProvider(content: @Composable () -> Unit) {
    val handler = rememberFixaverseUriHandler()
    CompositionLocalProvider(LocalUriHandler provides handler) {
        content()
    }
}

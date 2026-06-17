package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.UriHandler
import java.awt.Desktop
import java.net.URI

@Composable
actual fun rememberFixaverseUriHandler(): UriHandler = remember {
    object : UriHandler {
        override fun openUri(uri: String) {
            val url = normalizeUrl(uri)
            if (url.isEmpty()) return
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
            }
        }
    }
}

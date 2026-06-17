package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.UriHandler
import kotlinx.browser.window

@Composable
actual fun rememberFixaverseUriHandler(): UriHandler = remember {
    object : UriHandler {
        override fun openUri(uri: String) {
            val url = normalizeUrl(uri)
            if (url.isNotEmpty()) {
                window.open(url, "_blank")
            }
        }
    }
}

package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.UriHandler
import kotlinx.browser.window
import pro.fixaverse.data.citation.parseCitationUri

@Composable
actual fun rememberFixaverseUriHandler(): UriHandler = remember {
    object : UriHandler {
        override fun openUri(uri: String) {
            if (parseCitationUri(uri) != null) return
            val url = normalizeUrl(uri)
            if (url.isNotEmpty()) {
                window.open(url, "_blank")
            }
        }
    }
}

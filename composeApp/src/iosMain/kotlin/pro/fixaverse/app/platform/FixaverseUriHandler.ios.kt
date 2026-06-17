package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.UriHandler
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun rememberFixaverseUriHandler(): UriHandler = remember {
    object : UriHandler {
        override fun openUri(uri: String) {
            val url = normalizeUrl(uri)
            val nsUrl = NSURL.URLWithString(url) ?: return
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }
}
